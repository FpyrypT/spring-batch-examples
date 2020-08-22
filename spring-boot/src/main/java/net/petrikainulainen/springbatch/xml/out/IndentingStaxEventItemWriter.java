package net.petrikainulainen.springbatch.xml.out;

import javanet.staxutils.IndentingXMLEventWriter;
import javanet.staxutils.helpers.EventWriterDelegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.xml.StaxEventItemWriter;

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.Writer;

public class IndentingStaxEventItemWriter<T> extends StaxEventItemWriter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndentingStaxEventItemWriter.class);

    @Override
    protected XMLEventWriter createXmlEventWriter(XMLOutputFactory outputFactory, Writer writer) throws XMLStreamException {
        XMLEventWriter eventWriter = super.createXmlEventWriter(outputFactory, writer);
        IndentingXMLEventWriter indentingXMLEventWriter = new IndentingXMLEventWriter(eventWriter);
        return new XMLEventWriterFilteringBlankCharacterEvents(indentingXMLEventWriter);
    }

    @Override
    protected void endDocument(XMLEventWriter writer) throws XMLStreamException {
        XMLEventFactory factory = createXmlEventFactory();
        writer.add(factory.createEndElement(getRootTagNamespacePrefix(), getRootTagNamespace(), getRootTagName()));
        writer.add(factory.createEndDocument());
    }

    /**
     * See BATCH-2113
     */
    private static class XMLEventWriterFilteringBlankCharacterEvents extends EventWriterDelegate {

        public XMLEventWriterFilteringBlankCharacterEvents(XMLEventWriter out) {
            super(out);
        }

        public void add(XMLEvent event) throws XMLStreamException {
            if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                Characters characters = event.asCharacters();
                if (StringUtils.isBlank(characters.getData())) {
                    LOGGER.debug("Skipping blank event characters, {}", event);
                    return;
                }
            }
            super.add(event);
        }
    }

}
