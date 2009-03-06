/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of PieSpy.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: InferenceHeuristic.java,v 1.1 2004/05/10 10:13:53 pjm2 Exp $

*/

package org.jibble.socnet;

public abstract class InferenceHeuristic implements java.io.Serializable {
    
    public InferenceHeuristic(Graph graph, Configuration config) {
        _graph = graph;
        this.config = config;
        
        double weighting = 0;
        String className = this.toString();
        try {
            weighting = config.getDouble(className);
        }
        catch (Exception e) {
            System.out.println("Could not find a set weighting for " + className + ": " + e);
        }
        _weighting = weighting;
    }
    
    public double getHeuristicWeighting() {
        return _weighting;
    }
    
    public Graph getGraph() {
        return _graph;
    }
    
    public Configuration getConfig() {
        return config;
    }
    
    public String toString() {
        return this.getClass().getName();
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
    public boolean equals(Object o) {
        return toString().equals(o);
    }
    
    public abstract void infer(String nick, String message);
    
    private double _weighting;
    private Graph _graph;
    private Configuration config;
}