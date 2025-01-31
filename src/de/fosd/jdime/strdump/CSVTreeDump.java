package de.fosd.jdime.strdump;
import de.fosd.jdime.artifact.Artifact;
import de.fosd.jdime.config.merge.MergeScenario;
import de.fosd.jdime.config.merge.Revision;
import de.fosd.jdime.matcher.matching.Color;
import de.fosd.jdime.matcher.matching.Matching;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.util.function.Function;

/**
 * Copyright (C) 2013-2014 Olaf Lessenich
 * Copyright (C) 2014-2018 University of Passau, Germany
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 * Contributors:
 *     Olaf Lessenich <lessenic@fim.uni-passau.de>
 *     Georg Seibt <seibt@fim.uni-passau.de>
 */

/**
 * Dumps the given <code>Artifact</code> tree as CSV FILES.
 */

public class CSVTreeDump implements StringDumper {

    private static final String LS = System.lineSeparator();
    private Map<String, Integer> renumber = new HashMap<String,Integer>();
    private Integer nr_inc = -1;

    private void incNr(){
        nr_inc += 1;
    }
    private int getNr(){
        return nr_inc;
    }

    /**
     * Appends a plaintext representation of the tree with <code>artifact</code> at its root to the given
     * <code>builder</code>.
     *
     * @param artifact
     *         the <code>Artifact</code> to dump
     * @param getLabel
     *         the <code>Function</code> to use for producing a label an <code>Artifact</code>
     * @param builder
     *         the <code>StringBuilder</code> to append to
     * @param <T>
     *         the type of the <code>Artifact</code>
     */
    private <T extends Artifact<T>> void dumpTree(Artifact<T> artifact, Function<Artifact<T>, String> getLabel
                                                 , StringBuilder builder, String parentID) {

        if (artifact == null) {
            builder.append("NONE");
            builder.append(LS);
            return;
        }

        if (artifact.isChoice() || artifact.isConflict()) {


            //builder.append(Color.RED.toShell());

            if (artifact.isChoice()) {
                // isChoice() means that we insert something?
                appendArtifact(artifact, getLabel, builder, "fake:0"); builder.append(LS);

                for (Map.Entry<String, T> entry : artifact.getVariants().entrySet()) {
                    builder.append("#ifdef ").append(entry.getKey()).append(LS);
                    dumpTree(entry.getValue(), getLabel, builder,parentID);
                    builder.append("#endif").append(LS);
                }
            } else if (artifact.isConflict()) {
                Artifact<T> left = artifact.getLeft();
                Artifact<T> right = artifact.getRight();

                dumpTree(left, getLabel, builder,parentID);
                dumpTree(right, getLabel, builder,parentID);
            }
            return;
        }

        if (artifact.hasMatches()) {
            Iterator<Map.Entry<Revision, Matching<T>>> it = artifact.getMatches().entrySet().iterator();
            Matching<T> firstEntry = it.next().getValue();


            artifact.setRevision(MergeScenario.TARGET);
            appendArtifact(artifact, getLabel, builder, parentID);

        } else {
            // handle insertion of new lines
            appendArtifact(artifact, getLabel, builder, parentID);
        }

        builder.append(LS);
        //rekursion mit dumpTree()
        String id = artifact.getId();

        for (Iterator<T> it = artifact.getChildren().iterator(); it.hasNext(); ) {
            Artifact<T> next = it.next();
            dumpTree(next, getLabel, builder, id);

        }
        //throw new RuntimeException("test");
    }


    /**
     * Appends the representation of the given <code>Artifact</code> to the <code>builder</code>.
     *
     * @param artifact
     *         the <code>Artifact</code> to append to the <code>builder</code>
     * @param getLabel
     *         the <code>Function</code> to use for producing a label for the <code>Artifact</code>
     * @param builder
     *         the <code>StringBuilder</code> to append to
     * @param <T>
     *         the type of the <code>Artifact</code>
     */
    private <T extends Artifact<T>> void appendArtifact(Artifact<T> artifact, Function<Artifact<T>, String> getLabel,
                                                        StringBuilder builder, String parentID) {
        // NodeNr, side, Type, ID, Package,  ParentNodeNr, ParentSide
        String id = artifact.getId();
        String[] parId = parentID.split(":");

        Integer nr = getNr();
        incNr();
        renumber.put(id,nr);
        Integer par_nr = renumber.get(parentID);
        String side = id.split(":")[0];

        builder.append(nr).append(",");
        // !!! Uncomment this line if you want to see the side of the artifact
        builder.append(side).append(",");

        String[] type = getLabel.apply(artifact).split(" ");

        int len = type.length;
        // make sure that the same amount of entries are done into the table
        // assume that ID is the second entry, if it exists
        if (len > 3 ){
            throw new IndexOutOfBoundsException("Label contains more than 3 fields" + type.toString());
        }
        String TYPE = type[0];
        String ID = "";
        String LITERAL = "";
        for (int i = 1; i <= 3 && i < len; i++) {

            if(type[i].contains("ID="))
                ID = type[i].substring(type[i].indexOf("ID=") + 3).replace("\"","");
            if(type[i].contains("LITERAL="))
                LITERAL = type[i].substring(type[i].indexOf("LITERAL=") + 8).replace("\"", "");
        }
        builder.append(TYPE).append(",").append(ID).append(",").append(LITERAL).append(","); //.append(Package).append(",");
        try {
            builder.append(par_nr).append(",").append(parId[0]);
        }catch (ArrayIndexOutOfBoundsException e){
            builder.append("Indexoutofbounds").append(parentID);
        }

    }


    /**
     * Replicates the given <code>String</code> <code>n</code> times and returns the concatenation.
     *
     * @param s
     *         the <code>String</code> to replicate
     * @param n
     *         the number of replications
     * @return the concatenation
     */
    private static String replicate(String s, int n) {
        return new String(new char[n]).replace("\0", s).intern();
    }

    @Override
    public <T extends Artifact<T>> String dump(Artifact<T> artifact, Function<Artifact<T>, String> getLabel) {
        StringBuilder builder = new StringBuilder();
        String id = "target:" + String.valueOf(getNr());
        renumber.put(id,getNr());
        incNr();
        dumpTree(artifact, getLabel, builder,id);

        /*int lastLS = builder.lastIndexOf(LS);

        if (lastLS != -1) {
            builder.delete(lastLS, lastLS + LS.length());
        }*/
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter("TargetAST.csv");
            csvWriter.write(builder.toString());
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }
}

