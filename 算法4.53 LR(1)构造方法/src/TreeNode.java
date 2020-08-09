
import java.util.LinkedList;

public interface TreeNode {
    InnerNode getMother();
    LinkedList<TreeNode> getChildren();
    void setMother(InnerNode in);
    TreeType treeType();
}

class InnerNode implements TreeNode {
    private static class Empty extends InnerNode {
        Empty() {}
        public InnerNode getMother() {return null; }
        public LinkedList<TreeNode> getChildren() {return null; }

        @Override
        public String toString() {
            return "";
        }
    }

    InnerNode() {}
    InnerNode(InnerNode m) {mother = m; }
    InnerNode(Nonterminal o) {original = o;}
    void addChild(TreeNode child) {
        children.addFirst(child);
        if (child!=empty) child.setMother(this);
    }
    void addChildLast(TreeNode child) {
        children.addLast(child);
        if (child!=empty) child.setMother(this);
    }
    public InnerNode getMother() {return mother; }
    public LinkedList<TreeNode> getChildren() {return children; }

    @Override
    public void setMother(InnerNode inner) {
        mother = inner;
    }

    @Override
    public TreeType treeType() {
        return TreeType.INNER;
    }

    public void setOriginal(Nonterminal n) {original = n; }

    /*public ArrayList<WordWithCode> getSentence() {
        return production;
    }*/
    public Nonterminal getOriginal() {return original; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(original).append("---->");
        for (TreeNode node: children) {
            if (node.treeType()!=TreeType.INNER) builder.append(node);
            else builder.append(((InnerNode) node).original);
            builder.append(" , ");
        }
        return builder.toString();
    }

    protected InnerNode mother;
    protected LinkedList<TreeNode> children = new LinkedList<>();
    protected Nonterminal original;
    //protected ArrayList<WordWithCode> production;
    static Empty empty = new Empty();
}

class Root extends InnerNode {
    Root() {
        super();
        mother = empty;
    }

    @Override
    public InnerNode getMother() {
        return null;
    }

    @Override
    public LinkedList<TreeNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return children.get(0).toString();
    }
}

class Leaf implements TreeNode {
    Leaf(InnerNode m,Terminal t) {mother = m;term = t;  }
    Leaf(Terminal t) {term = t;  }
    @Override
    public InnerNode getMother() {
        return mother;
    }

    @Override
    public LinkedList<TreeNode> getChildren() {
        return null;
    }

    @Override
    public void setMother(InnerNode inner) {
        mother = inner;
    }

    @Override
    public TreeType treeType() {
        return TreeType.LEAF;
    }

    @Override
    public String toString() {
        return term.toString();
    }

    InnerNode mother;
    Terminal term;
}

class ActionNode implements TreeNode {
    ActionNode(Code fun) {function = fun; }

    @Override
    public InnerNode getMother() {
        return mother;
    }

    @Override
    public LinkedList<TreeNode> getChildren() {
        return null;
    }

    @Override
    public void setMother(InnerNode in) {
        mother = in;
    }

    @Override
    public TreeType treeType() {
        return TreeType.FUNCTION;
    }

    Code function;
    InnerNode mother;
}

class Tree {
    Tree(Root r) {root = r; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        LinkedList<InnerNode> stack = new LinkedList<>() {{push((InnerNode) root.children.getFirst());}};
        while (!stack.isEmpty()) {
            InnerNode now = stack.pop();
            builder.append(now).append("\n");
            for (TreeNode node: now.getChildren())
                if (node.treeType()==TreeType.INNER)
                    stack.add((InnerNode) node);
        }
        return builder.toString();
    }

    Root root;
}