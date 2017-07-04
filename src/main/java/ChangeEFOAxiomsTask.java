import org.semanticweb.owlapi.model.*;

/**
 * Created by olgavrou on 29/06/2017.
 */
public class ChangeEFOAxiomsTask {

    public static void main(String [] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        if(args.length < 1){
            System.out.println("first argument needs to be path for new-efo.owl to be saved");
            System.exit(1);
        }
        //efo location
        //has_disease_location
        //disease term
        //part_of
        //anatomy basic component term
              AccessOntology accessEFO = new AccessOntology("http://www.ebi.ac.uk/efo/efo.owl",
                      "http://www.ebi.ac.uk/efo/EFO_0000784",
                      "http://www.ebi.ac.uk/efo/EFO_0000408",
                      "http://purl.obolibrary.org/obo/BFO_0000050",
                      "http://www.ebi.ac.uk/efo/EFO_0000786");

              accessEFO.changeAxioms();

              String path = args[0];
              if(path.endsWith("/")){
                path = path + "new-efo.owl";
              } else {
                  path = path + "/new-efo.owl";
              }
              accessEFO.saveNewOntology(path);
    }
}
