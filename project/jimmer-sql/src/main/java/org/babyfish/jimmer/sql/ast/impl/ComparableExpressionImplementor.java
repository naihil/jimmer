package org.babyfish.jimmer.sql.ast.impl;

import org.babyfish.jimmer.sql.ast.ComparableExpression;
import org.babyfish.jimmer.sql.ast.Predicate;

interface ComparableExpressionImplementor<T extends Comparable<T>> extends ComparableExpression<T> {

    @Override
    default Predicate lt(ComparableExpression<T> other) {
        return null;
    }

    @Override
    default Predicate lt(T other) {
        return null;
    }

    @Override
    default Predicate le(ComparableExpression<T> other) {
        return null;
    }

    @Override
    default Predicate le(T other) {
        return null;
    }

    @Override
    default Predicate gt(ComparableExpression<T> other) {
        return null;
    }

    @Override
    default Predicate gt(T other) {
        return null;
    }

    @Override
    default Predicate ge(ComparableExpression<T> other) {
        return null;
    }

    @Override
    default Predicate ge(T other) {
        return null;
    }
}