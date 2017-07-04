import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by olgavrou on 03/07/2017.
 */
public class AccessOntology {

    private OWLOntologyManager manager;
    private Set<OWLAxiom> addAxioms;
    private Set<OWLAxiom> removeAxioms;
    private OWLOntology ontology;
    private OWLReasonerFactory reasonerFactory;
    private String ontologyLocation;
    private String hasDiseaseLocation;
    private String diseaseTerm;
    private String partOf;
    private String anatomyBasicComponentTerm;
    private OWLReasoner reasoner;
    private AccessAxiomStructure axiomStructure;

    public AccessOntology(String efoLocation, String hasDiseaseLocation, String diseaseTerm,
                          String partOf, String anatomyBasicComponentTerm) throws OWLOntologyCreationException {
        this.ontologyLocation = efoLocation;
        this.hasDiseaseLocation = hasDiseaseLocation;
        this.diseaseTerm = diseaseTerm;
        this.partOf = partOf;
        this.anatomyBasicComponentTerm = anatomyBasicComponentTerm;
        manager = OWLManager.createOWLOntologyManager();
        addAxioms = new HashSet<OWLAxiom>();
        removeAxioms = new HashSet<OWLAxiom>();
        ontology = manager.loadOntologyFromOntologyDocument(IRI.create(this.ontologyLocation));
        reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences();
        axiomStructure = new AccessAxiomStructure(ontology, reasoner, this.partOf, this.anatomyBasicComponentTerm);
    }

    public void changeAxioms(){
        for(OWLClass owlClass : ontology.getClassesInSignature()){
            OWLClassImpl owlClassImpl = (OWLClassImpl) owlClass;

            if(!axiomStructure.belongsToTerm(owlClassImpl, this.diseaseTerm)){ //disease term
                continue; //to next class
            }

            for(OWLAxiom axiom : ontology.getAxioms(owlClass, null)){
                //if axiom is subClassOf has_disease_location get filler expression of has_disease_location
                if (axiomStructure.isSubClassOf_has_disease_locationSome(axiom, this.hasDiseaseLocation)){
                    OWLClassExpression fillerExpresion = axiomStructure.getFillerOfisSubClassOf_has_disease_locationSome(axiom);

                    if(fillerExpresion instanceof OWLObjectUnionOf){
                        OWLObjectUnionOf filler = (OWLObjectUnionOf) fillerExpresion;
                        OWLClass anatomyBasicComponent = null;
                        for(OWLClassExpression operand : filler.getOperands()){
                            if(operand instanceof OWLClass){
                                anatomyBasicComponent = (OWLClass) operand;
                            } else if (operand instanceof OWLObjectSomeValuesFrom){
                                if(axiomStructure.isPartOfSomeAnatomyBasicComponent((OWLObjectSomeValuesFrom) operand)){
                                    OWLClass anatomyBasicComp = axiomStructure.getABCisPartOfSomeAnatomyBasicComp((OWLObjectSomeValuesFrom) operand);
                                    if(anatomyBasicComp.getIRI().toString().equals(anatomyBasicComponent.getIRI().toString())){
                                        //remove the axiom
//                                            RemoveAxiom removeAxiom = new RemoveAxiom(ontology, axiom);
                                        removeAxioms.add(axiom);


                                        OWLSubClassOfAxiomImpl subClassOfAxiom = (OWLSubClassOfAxiomImpl) axiom;
                                        OWLObjectSomeValuesFromImpl someValuesFrom = (OWLObjectSomeValuesFromImpl) subClassOfAxiom.getSuperClass();

                                        OWLObjectSomeValuesFromImpl newAxiomSuperClass = new OWLObjectSomeValuesFromImpl(someValuesFrom.getProperty(), anatomyBasicComp);
                                        OWLAxiom newOwlAxiom = new OWLSubClassOfAxiomImpl(subClassOfAxiom.getSubClass(),
                                                newAxiomSuperClass, subClassOfAxiom.getAnnotations());

//                                            AddAxiom addAxiom = new AddAxiom(ontology, owlAxiom);
                                        addAxioms.add(newOwlAxiom);

                                        System.out.println(owlClass.getIRI().toString());

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void saveNewOntology(String newEfoLocation) throws OWLOntologyStorageException {
        manager.removeAxioms(ontology, removeAxioms);
        manager.addAxioms(ontology, addAxioms);

        File file = new File(newEfoLocation);
        manager.saveOntology(ontology, manager.getOntologyFormat(ontology), IRI.create(file.toURI()));
    }


    public OWLOntologyManager getManager() {
        return manager;
    }

    public void setManager(OWLOntologyManager manager) {
        this.manager = manager;
    }

    public Set<OWLAxiom> getAddAxioms() {
        return addAxioms;
    }

    public void setAddAxioms(Set<OWLAxiom> addAxioms) {
        this.addAxioms = addAxioms;
    }

    public Set<OWLAxiom> getRemoveAxioms() {
        return removeAxioms;
    }

    public void setRemoveAxioms(Set<OWLAxiom> removeAxioms) {
        this.removeAxioms = removeAxioms;
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public void setReasonerFactory(OWLReasonerFactory reasonerFactory) {
        this.reasonerFactory = reasonerFactory;
    }

    public String getOntologyLocation() {
        return ontologyLocation;
    }

    public void setOntologyLocation(String ontologyLocation) {
        this.ontologyLocation = ontologyLocation;
    }

    public String getHasDiseaseLocation() {
        return hasDiseaseLocation;
    }

    public void setHasDiseaseLocation(String hasDiseaseLocation) {
        this.hasDiseaseLocation = hasDiseaseLocation;
    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public void setReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
    }

    public AccessAxiomStructure getAxiomStructure() {
        return axiomStructure;
    }

    public void setAxiomStructure(AccessAxiomStructure axiomStructure) {
        this.axiomStructure = axiomStructure;
    }

    public String getDiseaseTerm() {
        return diseaseTerm;
    }

    public void setDiseaseTerm(String diseaseTerm) {
        this.diseaseTerm = diseaseTerm;
    }

    public String getPartOf() {
        return partOf;
    }

    public void setPartOf(String partOf) {
        this.partOf = partOf;
    }

    public String getAnatomyBasicComponentTerm() {
        return anatomyBasicComponentTerm;
    }

    public void setAnatomyBasicComponentTerm(String anatomyBasicComponentTerm) {
        this.anatomyBasicComponentTerm = anatomyBasicComponentTerm;
    }
}
