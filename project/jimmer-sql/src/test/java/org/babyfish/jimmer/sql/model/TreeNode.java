package org.babyfish.jimmer.sql.model;

import org.babyfish.jimmer.sql.CascadeAction;
import org.babyfish.jimmer.sql.OnCascade;

import javax.persistence.*;
import java.util.List;

@Entity
public interface TreeNode {

    String name();

    @ManyToOne
    @OnCascade(CascadeAction.DELETE)
    TreeNode parent();

    @OneToMany(mappedBy = "parent")
    List<TreeNode> childNodes();

    /*
     * Set the id to be last field,
     * to test whether apt always generate it at first
     * (hashCode/equals requires it)
     */
    @Id
    @Column(name = "NODE_ID", nullable = false)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sequence:tree_node_id_seq"
    )
    long id();
}