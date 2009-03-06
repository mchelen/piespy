/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of PieSpy.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: AdjacencyInferenceHeuristic.java,v 1.2 2004/05/10 13:04:16 pjm2 Exp $

*/

package org.jibble.socnet;

public class AdjacencyInferenceHeuristic extends InferenceHeuristic implements java.io.Serializable {
    
    public AdjacencyInferenceHeuristic(Graph g, Configuration config) {
        super(g, config);
    }
    
    public void infer(String nick, String message) {
        
        Graph g = getGraph();
        Configuration config = getConfig();
        double weighting = getHeuristicWeighting();
        
        if (_lastNick != null) {
            //g.setCaption(this.toString() + " awarded weighting of " + getHeuristicWeighting() + " to " + nick + " - " + _lastNick);
            g.addEdge(new Node(nick), new Node(_lastNick), getHeuristicWeighting());
        }
        
        _lastNick = nick;
    }
    
    private String _lastNick = null;
    
}