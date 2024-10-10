package org.babyfish.jimmer.spring.repo.support

import org.babyfish.jimmer.Input
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.Slice
import org.babyfish.jimmer.View
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.spring.repo.KotlinRepository
import org.babyfish.jimmer.spring.repo.PageParam
import org.babyfish.jimmer.spring.repository.JRepository
import org.babyfish.jimmer.spring.repository.orderBy
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.fetcher.DtoMetadata
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.mutation.KBatchSaveResult
import org.babyfish.jimmer.sql.kt.ast.mutation.KSaveCommandDsl
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.ast.query.SortDsl
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass

/**
 * The base implementation of [KotlinRepository]
 */
abstract class AbstractKotlinRepository<E: Any, ID: Any>(
    protected val sqlClient: KSqlClient
) : KotlinRepository<E, ID> {

    @Suppress("UNCHECKED_CAST")
    protected val entityType: KClass<E> =
        GenericTypeResolver
            .resolveTypeArguments(this.javaClass, JRepository::class.java)
            ?.let { it[0].kotlin as KClass<E> }
            ?: throw IllegalArgumentException(
                "The class \"" + this.javaClass + "\" " +
                    "does not explicitly specify the type arguments of \"" +
                    JRepository::class.java.name +
                    "\" so that the entityType must be specified"
            )

    protected val immutableType: ImmutableType =
        ImmutableType.get(this.entityType.java)

    override fun findById(id: ID, fetcher: Fetcher<E>?): E? =
        if (fetcher == null) {
            sqlClient.findById(entityType, id)
        } else {
            sqlClient.findById(fetcher, id)
        }

    override fun <V : View<E>> findById(id: ID, viewType: KClass<V>): V? =
        sqlClient.findById(viewType, id)

    override fun findByIds(ids: Collection<ID>, fetcher: Fetcher<E>?): List<E> =
        if (fetcher == null) {
            sqlClient.findByIds(entityType, ids)
        } else {
            sqlClient.findByIds(fetcher, ids)
        }

    override fun <V : View<E>> findByIds(ids: Collection<ID>, viewType: KClass<V>): List<V> =
        sqlClient.findByIds(viewType, ids)

    override fun findMapByIds(ids: Collection<ID>, fetcher: Fetcher<E>?): Map<ID, E> =
        if (fetcher == null) {
            sqlClient.findMapByIds(entityType, ids)
        } else {
            sqlClient.findMapByIds(fetcher, ids)
        }

    @Suppress("UNCHECKED_CAST")
    override fun <V : View<E>> findMapByIds(ids: Collection<ID>, viewType: KClass<V>): Map<ID, V> =
        DtoMetadata.of(viewType.java).let { metadata ->
            val idPropId = immutableType.idProp.id
            sqlClient.findByIds(metadata.fetcher, ids).associateBy({
                (it as ImmutableSpi).__get(idPropId) as ID
            }) {
                metadata.converter.apply(it)
            }
        }

    override fun findAll(fetcher: Fetcher<E>?, block: (SortDsl<E>.() -> Unit)?): List<E> =
        if (fetcher == null) {
            sqlClient.entities.findAll(entityType, block)
        } else {
            sqlClient.entities.findAll(fetcher, block)
        }

    override fun <V : View<E>> findAll(viewType: KClass<V>, block: (SortDsl<E>.() -> Unit)?): List<V> =
        sqlClient.entities.findAllViews(viewType, block)

    override fun findPage(
        pageParam: PageParam,
        fetcher: Fetcher<E>?,
        block: (SortDsl<E>.() -> Unit)?
    ): Page<E> =
        sqlClient.createQuery(entityType) {
            orderBy(block)
            select(table.fetch(fetcher))
        }.fetchPage(pageParam.index, pageParam.size)

    override fun <V : View<E>> findPage(
        pageParam: PageParam,
        viewType: KClass<V>,
        block: (SortDsl<E>.() -> Unit)?
    ): Page<V> =
        sqlClient.createQuery(entityType) {
            orderBy(block)
            select(table.fetch(viewType))
        }.fetchPage(pageParam.index, pageParam.size)

    override fun findSlice(
        limit: Int,
        offset: Int,
        fetcher: Fetcher<E>?,
        block: (SortDsl<E>.() -> Unit)?
    ): Slice<E> =
        sqlClient.createQuery(entityType) {
            orderBy(block)
            select(table.fetch(fetcher))
        }.fetchSlice(limit, offset)

    override fun <V : View<E>> findSlice(
        limit: Int,
        offset: Int,
        viewType: KClass<V>,
        block: (SortDsl<E>.() -> Unit)?
    ): Slice<V> =
        sqlClient.createQuery(entityType) {
            orderBy(block)
            select(table.fetch(viewType))
        }.fetchSlice(limit, offset)

    override fun saveEntity(entity: E, block: (KSaveCommandDsl.() -> Unit)?): KSimpleSaveResult<E> =
        if (block == null) {
            sqlClient.save(entity)
        } else {
            sqlClient.save(entity, block)
        }

    override fun saveEntities(entities: Collection<E>, block: (KSaveCommandDsl.() -> Unit)?): KBatchSaveResult<E> =
        sqlClient.entities.saveEntities(entities, null, block)

    override fun saveInput(input: Input<E>, block: (KSaveCommandDsl.() -> Unit)?): KSimpleSaveResult<E> =
        sqlClient.entities.save(input, null, block)

    override fun saveInputs(inputs: Collection<Input<E>>, block: (KSaveCommandDsl.() -> Unit)?): KBatchSaveResult<E> =
        sqlClient.entities.saveInputs(inputs, null, block)

    override fun deleteById(id: ID, deleteMode: DeleteMode): Int =
        sqlClient.deleteById(entityType, id, deleteMode).affectedRowCount(entityType)

    override fun deleteByIds(ids: Collection<ID>, deleteMode: DeleteMode): Int =
        sqlClient.deleteByIds(entityType, ids, deleteMode).affectedRowCount(entityType)
}