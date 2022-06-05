import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.XMLParser;

import org.json.XML;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Command(name = "hl72json", version = "HL72JJson 1.0", mixinStandardHelpOptions = true)
public class Hl72json implements Runnable {

    @Option(names = { "--xml" }, description = "Output as xml") boolean xml;

    @Option(names = { "--json" }, description = "Output as json") boolean json;

    @Option(names = { "--replace-linebreaks" }, description = "Replace linefeeds/ linebreaks (\\r\\n or \\n) with \\r. Turning this off may break hl7 decoding")
    boolean replaceLinebreaks = true;

    @Option(names = { "--file" }, description = "HL7 file to be converted") String filepath = null;

    @Override
    public void run() {
        if (xml && json) {
            throw new RuntimeException("Illegal arguments: Cannot convert to both xml and json");
        }
        if (!xml && !json) {
            xml = true; // take xml as default output format
        }

        String msg = null;
        if (filepath != null) {
            try {
                 msg = Files.readString(Path.of(filepath));
            } catch (IOException e) {
                throw new RuntimeException("Error reading string from file " + filepath);
            }
        }

        if (replaceLinebreaks) {
            // replace possible wrong line feeds/ linebreaks, hl7 is picky with them
            msg = msg.replace("\r\n", "\r").replace('\n', '\r');
        }

        HapiContext context = new DefaultHapiContext();
        Parser p = context.getGenericParser();
        String xmlMessage;
        try {
            Message hapiMsg = p.parse(msg);
            XMLParser xmlParser = new DefaultXMLParser();
            xmlMessage = xmlParser.encode(hapiMsg);
        } catch (HL7Exception e) {
            throw new RuntimeException(e);
        }

        if (xml) {
            System.out.println(xmlMessage);
        }
        if (json) {
            System.out.println(XML.toJSONObject(xmlMessage).toString(4));
        }
    }

    /*
          System.out.println("----- ExampleMain started -------");
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
          String line = null;
          while (true) {
              if ((line = reader.readLine()) != null) {
                  System.out.println("echo>> " + line);
              } else {
                  //input finishes
                  break;
              }
          }
      } catch (Exception e) {
          System.err.println(e);
      }
     */

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Hl72json()).execute(args);
        System.exit(exitCode);
    }
}
