package unidue.ubo.enrichment;

import org.jdom2.Document;
import org.mycore.oai.pmh.Record;

public class OAIRecord {

    private Record record;

    private Document document;

    /**
     * @param record
     */
    public OAIRecord(Record record) {
        this.record = record;
    }

    /**
     * @return the record
     */
    public Record getRecord() {
        return record;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

}
