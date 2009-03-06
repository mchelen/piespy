/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of PieSpy.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: SocialNetworkBot.java,v 1.34 2004/05/11 17:07:37 pjm2 Exp $

*/

package org.jibble.socnet;

import org.jibble.pircbot.*;

import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * SocialNetworkBot extends PircBot to connect to IRC. It maintains a
 * collection of Graph objects - one per channel.
 */
public class SocialNetworkBot extends PircBot {

    public static final String VERSION = "PieSpy 0.4.0";

    public SocialNetworkBot(Configuration config) throws IOException {
        this.config = config;
        
        // Prevent construction if the output directory does not exist.
        if (!config.outputDirectory.exists() || !config.outputDirectory.isDirectory()) {
            throw new IOException("Output directory (" + config.outputDirectory + ") does not exist.");
        }
        
    }

    // Overriden from PircBot.    
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        
        if (config.ignoreSet.contains(sender.toLowerCase())) {
            return;
        }
        
        add(channel, sender);
        
        // Pass the message on to the InferenceHeuristics in the channel's Graph.
        String key = channel.toLowerCase();
        Graph graph = (Graph) _graphs.get(key);
        graph.infer(sender, Colors.removeFormattingAndColors(message));
    }
    
    // Overriden from PircBot. Private messages can control the bot.
    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        // Only allow access if the correct password has been supplied.
        if (!message.startsWith(config.password)) {
        //    disabled for twitter
        //    sendMessage(sender, "Incorrect password.");
            return;
        }
        
        message = message.substring(config.password.length()).trim();
        String messageLc = message.toLowerCase();
        
        if (messageLc.equals("stats")) {
            // Tell the user about the Graphs currently stored.
            Iterator keyIt = _graphs.keySet().iterator();
            while (keyIt.hasNext()) {
                String key = (String) keyIt.next();
                Graph graph = (Graph) _graphs.get(key);
                sendMessage(sender, key + ": " + graph.toString());
            }
        }
        else if (messageLc.startsWith("raw ")) {
            // Send a raw line to the IRC server.
            sendRawLine(message.substring(4));
        }
        else if (messageLc.startsWith("join ")) {
            // Join a new channel.
            joinChannel(message.substring(5));
        }
        else if (messageLc.startsWith("part ")) {
            // Part the specified channel.
            String channel = message.substring(5);
            partChannel(channel);
            _channelSet.remove(channel.toLowerCase());
        }
        else if (messageLc.startsWith("ignore ") || messageLc.startsWith("remove ")) {
            // Add a user to the IgnoreSet and remove them from all Graphs.
            String nick = message.substring(7);
            config.ignoreSet.add(nick.toLowerCase());
            Iterator graphIt = _graphs.values().iterator();
            while (graphIt.hasNext()) {
                Graph g = (Graph) graphIt.next();
                boolean changed = g.removeNode(new Node(nick));
                if (changed) {
                    g.makeNextImage();
                }
            }
        }
        else if (messageLc.startsWith("draw ")) {
            // DCC SEND the latest file for a channel.
            StringTokenizer tokenizer = new StringTokenizer(message.substring(5));
            if (tokenizer.countTokens() >= 1) {
                String channel = tokenizer.nextToken();

                Graph graph = (Graph) _graphs.get(channel.toLowerCase());
                if (graph != null) {
                    try {
                        File file = (File) graph.getLastFile();
                        if (file != null) {
                            sendMessage(sender, "Trying to send \"" + file.getName() + "\"... If you have difficultly in recieving this file via DCC, there may be a firewall between us.");
                            dccSendFile(file, sender, 120000);
                        }
                        else {
                            sendMessage(sender, "I have not generated any images for " + channel + " yet.");
                        }
                    }
                    catch (Exception e) {
                        sendMessage(sender, "Sorry, mate: " + e.toString());
                    }
                }
                else {
                    sendMessage(sender, "Sorry, I don't know much about that channel yet.");
                }
            }
            else {
                sendMessage(sender, "Example of correct use is \"draw <#channel>\"");
            }
        }
        else {
            sendMessage(sender, "Sorry, I don't support that command yet.");
        }
    }
    
    // Overridden from PircBot. Treat channel actions as messages.
    protected void onAction(String sender, String login, String hostname, String target, String action) {
        if ("#&!+".indexOf(target.charAt(0)) >= 0) {
            onMessage(target, sender, login, hostname, action);
        }
    }

    // Overridden from PircBot.    
    protected void onJoin(String channel, String sender, String login, String hostname) {

        add(channel, sender);
        
        if (sender.equalsIgnoreCase(getNick())) {
            // Remember that we're meant to be in this channel
            _channelSet.add(channel.toLowerCase());
        }
    }
    
    // Overridden from PircBot.
    protected void onUserList(String channel, User[] users) {
        for (int i = 0; i < users.length; i++) {
            add(channel, users[i].getNick());
        }
    }
    
    // Overridden from PircBot.
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        add(channel, kickerNick);
        add(channel, recipientNick);
        
        if (recipientNick.equalsIgnoreCase(getNick())) {
            // The bot was kicked, so rejoin the channel (if possible).
            joinChannel(channel);
        }
    }
    
    // Overridden from PircBot.
    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        add(channel, sourceNick);
    }
    
    // Overridden from PircBot.
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        changeNick(oldNick, newNick);
    }
    
    // Overridden from PircBot.
    public void onDisconnect() {
        while (!isConnected()) {
            try {
                reconnect();
            }
            catch (Exception e) {
                try {
                    Thread.sleep(10*60*1000);
                }
                catch (InterruptedException ie) {
                    // do nothing
                }
            }
        }
        
        // We are now connected. Rejoin all channels.
        Iterator it = _channelSet.iterator();
        while (it.hasNext()) {
            joinChannel((String) it.next());
        }
    }
    
    private void add(String channel, String nick) {

        if (config.ignoreSet.contains(nick.toLowerCase())) {
            return;
        }

        Node node = new Node(nick);
        String key = channel.toLowerCase();
        
        // Create the Graph for this channel if it doesn't already exist.
        Graph graph = (Graph) _graphs.get(key);
        if (graph == null) {
            if (config.createRestorePoints) {
                graph = readGraph(key);
            }
            if (graph == null) {
                graph = new Graph(channel, config);
            }
            _graphs.put(key, graph);
        }
        
        // Add the Node to the Graph.
        graph.addNode(node);
    }

    private void changeNick(String oldNick, String newNick) {
        // Effect the nick change by calling the mergeNode method on all Graphs.
        Iterator graphIt = _graphs.values().iterator();
        while (graphIt.hasNext()) {
            Graph graph = (Graph) graphIt.next();
            Node oldNode = new Node(oldNick);
            Node newNode = new Node(newNick);
            graph.mergeNode(oldNode, newNode);
        }
    }
    
    // Read a serialized graph from disk.
    private Graph readGraph(String channel) {
        Graph g = null;
        // Try and see if the graph can be restored from file.
        try {
            String strippedChannel = channel.toLowerCase().substring(1);
            
            File dir = new File(config.outputDirectory, strippedChannel);
            File file = new File(dir, strippedChannel + "-restore.dat");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            String version = (String) ois.readObject();
            if (version.equals(SocialNetworkBot.VERSION)) {
                // Only read the object if the file is for the correct version.
                g = (Graph) ois.readObject();
            }
            ois.close();
        }
        catch (Exception e) {
            // Do nothing?
        }
        return g;
    }
    
    public Configuration getConfig() {
        return config;
    }
    
    public Graph getGraph(String channel) {
        channel = channel.toLowerCase();
        return (Graph) _graphs.get(channel);
    }
    
    public static void main(String[] args) throws Exception {
        
        Properties p = new Properties();
        String configFile = "./config.ini";
        if (args.length > 0) {
            configFile = args[0];
        }
        p.load(new FileInputStream(configFile));
        Configuration config = new Configuration(p);
        
        SocialNetworkBot bot = new SocialNetworkBot(config);
        bot.setVerbose(config.verbose);
        bot.setName(config.nick);
        bot.setLogin("piespy");
        bot.setVersion(VERSION + " http://www.jibble.org/piespy/");
        
        try {
            bot.setEncoding(config.encoding);
        }
        catch (UnsupportedEncodingException e) {
            // Stick with the platform default.
        }
        
        bot.connect(config.server, config.port, config.serverPassword);
        Iterator channelIt = config.channelSet.iterator();
        while (channelIt.hasNext()) {
            String channel = (String) channelIt.next();
            bot.joinChannel(channel);
        }
    }
    
    
    // HashMap of String -> Graph objects.
    private HashMap _graphs = new HashMap();

    // Used to remember which channels we should be in
    private HashSet _channelSet = new HashSet();

    private Configuration config;
    
}
