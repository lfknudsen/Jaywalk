package com.falkknudsen.osmunda;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.InputStream;

import static com.falkknudsen.jaywalk.util.Log.log;

/** Classes that would override {@link AbstractParser#parse(XMLStreamReader)} can extend
 this class instead to automatically use a {@link BufferedInputStream} around the stream
 which backs the {@link XMLStreamReader}. */
public abstract class AbstractBufferedParser extends AbstractParser {
    @Override
    public XMLStreamReader constructXMLReader(InputStream in) {
        try {
            log("Using XMLStreamReader from AbstractBufferedParser.");
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            return inputFactory.createXMLStreamReader(new BufferedInputStream(in), "UTF-8");
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractBufferedParser(String filename) {
        super(filename);
    }

    public AbstractBufferedParser() {
        super();
    }
}
