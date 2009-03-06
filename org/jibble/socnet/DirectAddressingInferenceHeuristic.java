/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of PieSpy.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: DirectAddressingInferenceHeuristic.java,v 1.2 2004/05/11 15:45:59 pjm2 Exp $

*/

package org.jibble.socnet;

public class DirectAddressingInferenceHeuristic extends InferenceHeuristic implements java.io.Serializable {
    
    public DirectAddressingInferenceHeuristic(Graph g, Configuration config) {
        super(g, config);
    }
    
    public void infer(String nick, String message) {
        
        Graph g = getGraph();
        Configuration config = getConfig();
        double weighting = getHeuristicWeighting();
        
        Node source = new Node(nick);
        String[] words = message.split("[\\s\\t\\n\\r\\f\\:\\.\\(\\)\\-\\,\\/\\&\\!\\?\"\"<>]+", 2);
        Node target = new Node(words[0]);
        if (g.contains(target)) {
            //g.setCaption(this.toString() + " awarded weighting of " + getHeuristicWeighting() + " to " + source + " - " + target);
            g.addEdge(source, target, getHeuristicWeighting());
        }
        
    }
    
}