package com.falkknudsen.osmunda;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/** Super-class for plain-text parsing. */
public abstract class AbstractParser {
    public static final int numOfNodes = 49493314;
    public static final int numOfWays = 49493314;
    public static final int numOfHighways = 49493314;
    public static final int maxMemberCount = 2912;

    /** The name of the file being parsed. It is a member variable to allow
     {@link #setup(String)} to be exposed.<br>
     Generally set when calling {@link #parse(String)}
     or {@link #setup(String)}, but can also be supplied during construction with {@link #AbstractParser(String)}
     if this is preferable. */
    protected String filename;

    public AbstractParser() {
        this.filename = "";
    }

    public AbstractParser(String filename) {
        this.filename = filename;
    }

    /** Parses the given file. Supports .txt, .osm, .zip, .tar, .gz, and .bz2.
     Nested compression is supported as well (e.g. .tar.gz).
     @return A relevant {@link DataContainer} with the collected data. */
    public DataContainer parse(String filename) throws IOException, XMLStreamException {
        this.filename = filename;
        XMLStreamReader reader = setup(filename);
        return parse(reader);
    }

    public DataContainer parse(InputStream in) throws IOException, XMLStreamException {
        return parse(constructXMLReader(in));
    }

    /** Unzip/decompress the input file as necessary, and return a {@link XMLStreamReader} to proceed with reading.<br>
     Supports .osm, .bz2, .zip, .tar, and .gz.<br>
     Supports nested compression (such as .tar.gz).*/
    public XMLStreamReader setup(String filename) throws IOException {
        this.filename = filename;
        InputStream stream = FileHandler.unpack(new File(filename)).snd();
        return constructXMLReader(stream);
    }

    /** Constructs the {@link XMLStreamReader} that will be used when parsing.
     Non-static so that it can be overridden. */
    public XMLStreamReader constructXMLReader(InputStream in) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            return inputFactory.createXMLStreamReader(in, "UTF-8");
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /** Parses an OSM file and returns an appropriate {@link DataContainer}. */
    public abstract DataContainer parse(XMLStreamReader in) throws XMLStreamException, IOException;
}
