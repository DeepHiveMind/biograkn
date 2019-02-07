package grakn.biograkn.migrator.pubmedarticle;

import ai.grakn.GraknTxType;
import ai.grakn.client.Grakn;
import ai.grakn.graql.Graql;
import ai.grakn.graql.InsertQuery;
import ai.grakn.graql.answer.ConceptMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static ai.grakn.graql.Graql.var;

public class PubmedArticle {

    public static void migrate(Grakn.Session session) {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get("dataset/disgenet/pmids.csv"));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

            for (CSVRecord csvRecord: csvParser) {

                // skip header
                if (csvRecord.getRecordNumber() == 1) {
                    continue;
                }

                double pmid = Double.parseDouble(csvRecord.get(0));
                String title = csvRecord.get(1);
                String articleAbstract = csvRecord.get(2);
                String url = csvRecord.get(3);

                InsertQuery insertQuery = Graql.insert(var("pa").isa("pubmed-article")
                        .has("pm-id", pmid)
                        .has("title", title)
                        .has("abstract", articleAbstract)
                        .has("url", url));

                Grakn.Transaction writeTransaction = session.transaction(GraknTxType.WRITE);
                List<ConceptMap> insertedIds = insertQuery.withTx(writeTransaction).execute();
                System.out.println("Inserted a pubmed article with ID: " + insertedIds.get(0).get("pa").id());
                writeTransaction.commit();
            }

            System.out.println("-----pubmed articles have been migrated-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
