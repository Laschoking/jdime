package de.fosd.jdime.artifact.ast;

import java.util.regex.Pattern;

import org.jastadd.extendj.ast.Block;
import org.jastadd.extendj.ast.List;
import org.jastadd.extendj.ast.Stmt;

import static java.util.regex.Pattern.MULTILINE;

/**
 * A {@link Block} that refers to the {@link SemiStructuredArtifact#getContent() content} of a
 * {@link SemiStructuredArtifact} for its pretty printing.
 */
public class SemiStructuredASTNode extends Block {

    private static final Pattern BRACES = Pattern.compile("\\A\\s*\\{\\s*$|\\R^\\s*\\}\\s*\\z", MULTILINE);

    private SemiStructuredArtifact artifact;

    /**
     * Constructs a new {@link SemiStructuredASTNode} referring to the given {@code artifact} for its pretty printing
     * via {@link SemiStructuredArtifact#getContent()}.
     *
     * @param artifact
     *         the {@link SemiStructuredArtifact} to refer to
     */
    public SemiStructuredASTNode(SemiStructuredArtifact artifact) {
        this.artifact = artifact;
    }

    /**
     * Sets the {@link SemiStructuredArtifact} to refer to the given {@code artifact}.
     *
     * @param artifact
     *         the new {@link SemiStructuredArtifact} to refer to for pretty printing
     */
    void setArtifact(SemiStructuredArtifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public void init$Children() {
        // Block adds an empty List as the first child. A SemiStructuredASTNode does not have children.
    }

    @Override
    protected int numChildren() {
        return 0; // Block returns 1...
    }

    @Override
    public void refined_PrettyPrint_Block_prettyPrint(StringBuffer sb) {
        sb.append(artifact.getContent().getContent().trim());
    }

    @Override
    public int getNumStmt() {
        return getNumStmtNoTransform();
    }

    @Override
    public int getNumStmtNoTransform() {
        return 1;
    }

    @Override
    public Stmt getStmt(int i) {
        /*
         * This Block can not be stored as a field because the SemiStructuredArtifact containing this
         * SemiStructuredASTNode (the 'artifact' field) is copied. This invalidates the reference used by the anonymous
         * class leading to incorrect content being printed.
         */
        return new Block() {

            @Override
            public void refined_PrettyPrint_Block_prettyPrint(StringBuffer sb) {
                sb.append(BRACES.matcher(artifact.getContent().getContent()).replaceAll(""));
            }
        };
    }

    @Override
    public List<Stmt> getStmtList() {
        return getStmtsNoTransform();
    }

    @Override
    public List<Stmt> getStmtListNoTransform() {
        return getStmtsNoTransform();
    }

    @Override
    public List<Stmt> getStmts() {
        return getStmtsNoTransform();
    }

    @Override
    public List<Stmt> getStmtsNoTransform() {
        return new List<>(getStmt(42));
    }
}
