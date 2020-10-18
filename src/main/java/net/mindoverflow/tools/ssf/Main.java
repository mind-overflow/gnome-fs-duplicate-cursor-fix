package net.mindoverflow.tools.ssf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.Map;

public class Main {

    private static int restartDelay;
    private static String monitorConnector;
    private static double monitorScale;
    private static boolean enable;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException, InterruptedException {
        File monitorConfig = new File(System.getProperty("user.home") + "/.config/monitors.xml");

        if(!monitorConfig.exists())
        {
            System.out.println("File \"monitors.xml\" does not exist! Quitting...");
            System.exit(1);
        }

        if(!loadConfig())
        {
            System.out.println("Error loading \"config.yml\"! Quitting...");
            System.exit(1);
        }

        if(!enable)
        {
            System.out.println("Script disabled! Enable it by setting \"enabled: true\" in config.yml!");
            System.exit(0);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(monitorConfig);

        Element rootElement = doc.getDocumentElement();
        System.out.println("Main element: " + rootElement.getNodeName());

        NodeList rootNodes = rootElement.getElementsByTagName("configuration");
        Node configuration = rootNodes.item(0);
        System.out.println("1 - Current node: " + configuration.getNodeName());
        NodeList logicalMonitors = configuration.getChildNodes();
        for(int pos = 0; pos < logicalMonitors.getLength(); pos++)
        {
            Node logicalMonitor = logicalMonitors.item(pos);
            if(!logicalMonitor.getNodeName().equalsIgnoreCase("logicalmonitor")) continue;
            System.out.println("    2 - Current node: " + logicalMonitor.getNodeName());
            Node scale = getNodeByName("scale", logicalMonitor.getChildNodes());
            System.out.println("        3A - current node: " + scale.getNodeName());
            System.out.println("            Current scale: " + scale.getTextContent());
            Node monitor = getNodeByName("monitor", logicalMonitor.getChildNodes());
            System.out.println("        3B - current node: " + monitor.getNodeName());
            Node monitorspec = getNodeByName("monitorspec", monitor.getChildNodes());
            System.out.println("            4 - current node: " + monitorspec.getNodeName());
            Node connector = getNodeByName("connector", monitorspec.getChildNodes());
            System.out.println("                    5 - current node: " + connector.getNodeName());
            System.out.println("                        Current connector: " + connector.getTextContent());

            String connectorValue = connector.getTextContent();

            if(connectorValue.equalsIgnoreCase(monitorConnector))
            {
                scale.setTextContent("1");
                saveFile(doc, monitorConfig);
                restartGnome();

                Thread.sleep(restartDelay);
                scale.setTextContent(monitorScale + "");
                saveFile(doc, monitorConfig);
                restartGnome();

                return;
            }

        }
    }

    private static Node getNodeByName(String name, NodeList list)
    {
        for(int pos = 0; pos < list.getLength(); pos++)
        {
            Node current = list.item(pos);
            if(current.getNodeName().equalsIgnoreCase(name)) return current;
        }

        return null;
    }

    private static void restartGnome() throws IOException, InterruptedException {
        String s;

        InputStream stream = Main.class.getResourceAsStream("/restart.sh");
        File script = new File("restart.sh");
        if(!script.exists())
        {
            System.out.println("Extracting restart script...");
            Files.copy(stream, script.getAbsoluteFile().toPath());
            System.out.println("Done!");
        }

        System.out.println("Restarting GNOME...");
        script.setExecutable(true);
        Process process = Runtime.getRuntime().exec(script.getAbsolutePath());

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

        while((s = br.readLine()) != null)
        {
            System.out.println("line: " + s);
        }

        process.waitFor();
        System.out.println ("exit: " + process.exitValue());
        process.destroy();
    }

    private static void saveFile(Document doc, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(file));
    }

    private static boolean loadConfig()
    {
        InputStream stream = Main.class.getResourceAsStream("/config.yml");
        File config = new File("config.yml");
        if(!config.exists())
        {
            System.out.println("Extracting config...");
            try
            {
                Files.copy(stream, config.getAbsoluteFile().toPath());
            } catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
            System.out.println("Done!");
        }

        System.out.println("Loading config...");
        Yaml configYaml = new Yaml();
        Map<String, Object> contents;
        try
        {
            contents = configYaml.load(new FileInputStream(config));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        enable = (boolean) contents.get("enable");
        restartDelay = (int) contents.get("delay");
        monitorScale = (double) contents.get("scale");
        monitorConnector = (String) contents.get("monitor-connector");
        System.out.println("Done!");
        System.out.println("Enabled: " + enable);
        System.out.println("Delay: " + restartDelay);
        System.out.println("Monitor name: " + monitorConnector);
        System.out.println("Monitor scale: " + monitorScale);
        return true;
    }
}
