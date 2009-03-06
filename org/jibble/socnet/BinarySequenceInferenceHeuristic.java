/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of PieSpy.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: BinarySequenceInferenceHeuristic.java,v 1.3 2004/05/10 23:34:21 pjm2 Exp $

*/

package org.jibble.socnet;

import java.util.*;

public class BinarySequenceInferenceHeuristic extends InferenceHeuristic implements java.io.Serializable {
    
    public static final int MIN_SEQ_SIZE = 5;
    
    public BinarySequenceInferenceHeuristic(Graph g, Configuration config) {
        super(g, config);
    }
    
    public void infer(String nick, String message) {
        
        Graph g = getGraph();
        Configuration config = getConfig();
        double weighting = getHeuristicWeighting();
        
        nickHistory.add(nick);
        if (nickHistory.size() > MIN_SEQ_SIZE) {
            nickHistory.removeFirst();
            Iterator nickIt = nickHistory.iterator();
            HashSet uniqueNicks = new HashSet();
            while (nickIt.hasNext()) {
                uniqueNicks.add(nickIt.next());
            }
            if (uniqueNicks.size() == 2) {
                // This means only two people were seen chatting over the
                // last MIN_SEQ_SIZE lines in this channel, so we can assume
                // they were talking to each other.
                Iterator setIt = uniqueNicks.iterator();
                String nick1 = (String) setIt.next();
                String nick2 = (String) setIt.next();
                //g.setCaption(this.toString() + " awarded weighting of " + getHeuristicWeighting() + " to " + nick1 + " - " + nick2);
                g.addEdge(new Node(nick1), new Node(nick2), getHeuristicWeighting());
                
                nickHistory.clear();
            }
        }
        
    }
    
    private LinkedList nickHistory = new LinkedList();
    
}