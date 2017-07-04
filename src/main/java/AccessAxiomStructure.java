import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by olgavrou on 03/07/2017.
 */
public class AccessAxiomStructure {

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private String partOf;
    private String anatomyBasicComponentTerm;

    public AccessAxiomStructure(OWLOntology ontology, OWLReasoner reasoner, String partOf, String anatomyBasicComponentTerm) {
        this.ontology = ontology;
        this.reasoner = reasoner;
        this.partOf = partOf;
        this.anatomyBasicComponentTerm = anatomyBasicComponentTerm;
    }

    public boolean isSubClassOf_has_disease_locationSome(OWLAxiom axiom, String hasDiseaseLocation){
        if (isSubClassOf(axiom)) {
            OWLClassExpression superClass = ((OWLSubClassOfAxiom) axiom).getSuperClass();
            if (superClass instanceof OWLObjectSomeValuesFrom) {
                Map<OWLObjectProperty, OWLClassExpression> someValues = getObjectsOfSomeValues((OWLObjectSomeValuesFrom) superClass);
                //disease
                OWLObjectProperty property = someValues.keySet().iterator().next();
                if (property.getIRI().toString().equals(hasDiseaseLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public OWLClassExpression getFillerOfisSubClassOf_has_disease_locationSome(OWLAxiom axiom){
        Map<OWLObjectProperty, OWLClassExpression> map = getObjectsOfSomeValues((OWLObjectSomeValuesFrom)((OWLSubClassOfAxiom) axiom).getSuperClass());
        return map.get(map.keySet().iterator().next());
    }

    public OWLClass getABCisPartOfSomeAnatomyBasicComp(OWLObjectSomeValuesFrom someValuesFrom) {
        Map<OWLObjectProperty, OWLClassExpression> someValues = getObjectsOfSomeValues(someValuesFrom);
        OWLClassExpression expression = someValues.get(someValues.keySet().iterator().next());
        return (OWLClass) expression;
    }

    public boolean isPartOfSomeAnatomyBasicComponent(OWLObjectSomeValuesFrom someValuesFrom){
        Map<OWLObjectProperty, OWLClassExpression> someValues = getObjectsOfSomeValues(someValuesFrom);

        //part_of
        OWLObjectProperty property = someValues.keySet().iterator().next();
        if (property.getIRI().toString().equals(this.partOf)){

            OWLClassExpression expression = someValues.get(property);
            if(expression instanceof OWLClass){
                OWLClass anatomyBasicComp = (OWLClass) expression;
                if (anatomyBasicComp.getIRI().toString().equals(anatomyBasicComp.getIRI().toString())){
                    //check to see if it is an anatomy basic component
                    if(belongsToTerm(anatomyBasicComp, this.anatomyBasicComponentTerm)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Map<OWLObjectProperty, OWLClassExpression> getObjectsOfSomeValues(OWLObjectSomeValuesFrom toBeSepareted){
        OWLObjectProperty propertyExpression = (OWLObjectProperty)toBeSepareted.getProperty();
        OWLClassExpression filler = toBeSepareted.getFiller();
        Map<OWLObjectProperty, OWLClassExpression> map = new HashMap<OWLObjectProperty, OWLClassExpression>();
        map.put(propertyExpression,filler);
        return map;
    }

    public boolean isSubClassOf(OWLAxiom axiom){
        if (axiom instanceof OWLSubClassOfAxiom) {
            return true;
        }
        return false;
    }

    public boolean belongsToTerm(OWLClass owlClass, String term){
        boolean isDeseaseClass = false;
        if(!isDirectSubclassOfTerm(owlClass, term)){
            //check parents
            Set<OWLClass> parents = getAllParents(owlClass);
            if (!parents.isEmpty()){
                for(OWLClass parent : parents){
                    if(isDirectSubclassOfTerm(parent, term)){
                        isDeseaseClass = true;
                        break;
                    }
                }
            }
        }
        return isDeseaseClass;
    }

    public boolean isDirectSubclassOfTerm(OWLClass owlClass, String term){
        for(OWLClassAxiom subClassAxiom : ontology.getAxioms(owlClass, null)){
            if(subClassAxiom instanceof OWLSubClassOfAxiomImpl){
                OWLClassExpression expression = ((OWLSubClassOfAxiomImpl) subClassAxiom).getSuperClass();
                if (expression instanceof OWLClassImpl){
                    OWLClassImpl subClass = (OWLClassImpl) expression;
                    if(subClass.getIRI().toString().equals(term)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Set<OWLClass> getAllParents(OWLClass owlClass){
        Set<OWLClass> parents = new HashSet<OWLClass>();

        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(owlClass, false);
        Set<OWLClass> flatSubclasses = superClasses.getFlattened();
        parents.addAll(flatSubclasses);
        return parents;
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
