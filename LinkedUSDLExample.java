
/* Copyright (C) 2012 Jorge Cardoso. All rights reserved.
* 
* This program and the accompanying materials are made available under
* the terms of the Common Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/cpl-v10.html
* 
* $Id: LinkedUSDLExample.java,v 0.1 2012/11/11 20:02:36 jorge_cardoso Exp $
*/

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

public class LinkedUSDLExample {

	public final static String USDL = "http://www.linked-usdl.org/ns/usdl#";
	public final static String RDF  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public final static String OWL  = "http://www.w3.org/2002/07/owl#";
	public final static String DC   = "http://purl.org/dc/elements/1.1/";
	public final static String XSD  = "http://www.w3.org/2001/XMLSchema#";
	public final static String VANN = "http://purl.org/vocab/vann/";
	public final static String FOAF = "http://xmlns.com/foaf/0.1/";
	public final static String USDK = "http://www.linked-usdl.org/ns/usdl#";
	public final static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public final static String GR   = "http://purl.org/goodrelations/v1#";
	public final static String SKOS = "http://www.w3.org/2004/02/skos/core#";
	public final static String ORG  = "http://www.w3.org/ns/org#";
	public final static String PRICE= "http://www.linked-usdl.org/ns/usdl-price#";
	public final static String LEGAL= "http://www.linked-usdl.org/ns/usdl-legal#";
	public final static String DEI  = "http://dei.uc.pt/rdf/dei#";

	public final static String USDL_Core_Schema_File = "./usdl-core.ttl";
	public final static String USDL_Price_Schema_File = "./usdl-price.ttl";
	public final static String USDL_Instance_File = "./Service_01.ttl";
				

    public static void main (String args[]) {
    	LinkedUSDLExample example = new LinkedUSDLExample();
    	
    	// create three empty models. One to store usdl-core, one to store usdl-price, and 
    	// one to store the actual service instance we will be creating
        Model usdlCoreSchemaModel = ModelFactory.createDefaultModel();
        Model usdlPriceSchemaModel = ModelFactory.createDefaultModel();
        Model usdlInstanceModel = ModelFactory.createDefaultModel();
    	
        // Create a usdl instance just to demonstrate how to model a service 
        // Here only usdl-core is used. Not all properties and classes were used
		usdlInstanceModel = example.populateUSDLmodel(usdlInstanceModel);
		
		// Add pricing information to the service instance created previously
		usdlInstanceModel = example.addUSDLPricing(usdlInstanceModel);
		
		// Add legal information to the service instance created previously
		// The code is missing for this method (comming soon)
		usdlInstanceModel = example.addUSDLLegal(usdlInstanceModel);
		
		// Display the usdl service instance just created
    	example.displayUSDLmodel(usdlInstanceModel, "TTL");
    	
		// Display the usdl service instance again by showing its triples
    	example.dumpUSDL(usdlInstanceModel);

    	// Write it to a file
		example.writeUSDLmodeltoFile(usdlInstanceModel, USDL_Instance_File, "TTL");
		
		// Load the usdl model. It should be available at linked-usdl.org
		// In this example, we use a local copy stored in disk
		usdlCoreSchemaModel = example.loadUSDLmodel(usdlCoreSchemaModel, USDL_Core_Schema_File, "TTL");

		// Check if the usdl instance we created complies with the model
		// Not sure if this function works well
		boolean valid = example.validateUSDLInstance(usdlCoreSchemaModel, usdlInstanceModel);
		if(valid) System.out.print("The usdl instance created is valid");
		else System.out.print("The usdl instance created is NOT valid!");
		
		// This function was called only once to make the file containing the price schema well formatted
		// example.prettyFormating(USDL_Price_Schema_File, "TTL");
    }

    public Model loadUSDLmodel(Model model, String filePath, String lang) {

        InputStream in = FileManager.get().open( filePath );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + filePath + " not found");
        }
        model.read(in, USDL, lang);
        return model;
    }

    
    public void displayUSDLmodel(Model model, String lang) {
    	// write it to standard out
    	model.write(System.out, lang, USDL);            
    }
    
    public boolean validateUSDLInstance(Model schema, Model model) {

		InfModel infmodel = ModelFactory.createRDFSModel(schema, model);
		ValidityReport validity = infmodel.validate();
		return validity.isValid();
    }
  
    public Model populateUSDLmodel(Model model) {

        Resource service 
          = model.createResource(DEI+"ServiceDesk").
            addProperty(model.createProperty(RDF+"type"), model.createResource(USDL+"Service")).
            addProperty(model.createProperty(DC+"title"), model.createLiteral("SugarCRM service instance", "en")).
            addProperty(model.createProperty(DC+"created"), ResourceFactory.createTypedLiteral("2012-11-11", XSDDatatype.XSDdate));

        Resource businessEntity 
        = model.createResource(DEI+"Service_Desk_Inc").
	        addProperty(model.createProperty(RDF+"type"), model.createResource(GR+"BusinessEntity")).
	        addProperty(model.createProperty(GR+"legalName"), model.createLiteral("Service Desk Inc.", "en")).
	        addProperty(model.createProperty(FOAF+"page"), model.createLiteral("<http://www.servicedesk.com/>")).
	        addProperty(model.createProperty(GR+"taxID"), ResourceFactory.createTypedLiteral(new Integer(345543234)));
    
        Resource participant 
          = model.createResource(DEI+"IT_Engineer").
	        addProperty(model.createProperty(RDF+"type"), model.createResource(USDL+"Participant")).
	        addProperty(model.createProperty(USDL+"hasAgent"), businessEntity).
	        addProperty(model.createProperty(USDL+"hasRole"),  model.createResource(USDL+"Provider"));
        
        Resource interactionPoint_Register 
          = model.createResource(DEI+"InteractionPoint_Register").
            addProperty(model.createProperty(RDF+"type"), model.createResource(USDL+"InteractionPoint")).
        	addProperty(model.createProperty(USDL+"receives"), model.createResource(DEI+"Form_850")).
        	addProperty(model.createProperty(USDL+"yields"), model.createResource(DEI+"Certificate")).		
    	    addProperty(model.createProperty(USDL+"hasInteractionSpace"), model.createLiteral("Remote", "en")).		
    	    addProperty(model.createProperty(USDL+"hasInteractionType"), model.createLiteral("Human-Machine", "en"));		

        Resource interactionPoint_Download 
          = model.createResource(DEI+"InteractionPoint_Download").
            addProperty(model.createProperty(RDF+"type"), model.createResource(USDL+"InteractionPoint")).
		    addProperty(model.createProperty(USDL+"receives"), model.createResource(DEI+"Password")).
		    addProperty(model.createProperty(USDL+"yields"), model.createResource(DEI+"Access")).		
	    	addProperty(model.createProperty(USDL+"hasInteractionSpace"), model.createLiteral("Remote", "en")).		
	    	addProperty(model.createProperty(USDL+"hasInteractionType"), model.createLiteral("Human-Machine", "en")).		
    		addProperty(model.createProperty(USDL+"hasParticipant"), participant);		

        
        service.addProperty(model.createProperty(USDL+"hasInteractionPoint"), interactionPoint_Register);
        service.addProperty(model.createProperty(USDL+"hasInteractionPoint"), interactionPoint_Download);

        Resource serviceOffer 
        = model.createResource(DEI+"ServiceDesk_Offering").
          addProperty(model.createProperty(RDF+"type"), model.createResource(USDL+"ServiceOffering")).
          addProperty(model.createProperty(USDL+"includes"), service);

        return model;  
    }

    public Model addUSDLPricing(Model model) {

        Resource quantitativeValue_MB
        = model.createResource(GR+"quantitativeValue_MB").
          addProperty(model.createProperty(RDF+"type"), model.createResource(GR+"QuantitativeValue")).
          addProperty(model.createProperty(GR+"hasUnitOfMeasurement"), model.createTypedLiteral("MB", XSDDatatype.XSDstring)).
          addProperty(model.createProperty(GR+"hasValue"), model.createTypedLiteral("1", XSDDatatype.XSDint));

        Resource unitPriceSpecification_MB
        = model.createResource(GR+"UnitPriceSpecification_MB").
          addProperty(model.createProperty(RDF+"type"), model.createResource(GR+"QuantitativeValue")).
          addProperty(model.createProperty(GR+"hasCurrency"), model.createTypedLiteral("USD", XSDDatatype.XSDstring)).
          addProperty(model.createProperty(GR+"hasCurrencyValue"), model.createTypedLiteral("9.90", XSDDatatype.XSDfloat)).
          addProperty(model.createProperty(GR+"validThrough"), model.createTypedLiteral("2012-12-31T23:59:59Z", XSDDatatype.XSDdate)).
          addProperty(model.createProperty(GR+"hasUnitOfMeasurement"), model.createTypedLiteral("MB", XSDDatatype.XSDstring));
        
        Resource priceComponent_Setup
        = model.createResource(PRICE+"priceComponent_Setup").
          addProperty(model.createProperty(RDF+"type"), model.createResource(PRICE+"PriceComponent")).
          addProperty(model.createProperty(PRICE+"hasMetrics"), quantitativeValue_MB).
          addProperty(model.createProperty(PRICE+"hasPrice"), unitPriceSpecification_MB);

        Resource priceComponent_MonthlyFee
        = model.createResource(PRICE+"priceComponent_MonthlyFee").
          addProperty(model.createProperty(RDF+"type"), model.createResource(PRICE+"PriceComponent")).
          addProperty(model.createProperty(PRICE+"hasMetrics"), quantitativeValue_MB).
          addProperty(model.createProperty(PRICE+"hasPrice"), unitPriceSpecification_MB);

        Resource pricePlan 
          = model.createResource(PRICE+"PricePlan_ServiceDesk").
            addProperty(model.createProperty(RDF+"type"), model.createResource(PRICE+"PricePlan")).
            addProperty(model.createProperty(PRICE+"hasPriceComponent"), priceComponent_Setup).
            addProperty(model.createProperty(PRICE+"hasPriceComponent"), priceComponent_MonthlyFee); 

        return model;  
    }    

    public Model addUSDLLegal(Model model) {
    	return model;
    }
    
	private void writeUSDLmodeltoFile(Model m, String filePath, String lang) {

		m.setNsPrefix("usdl",  USDL);
		m.setNsPrefix("rdf",   RDF);
		m.setNsPrefix("owl",   OWL);
		m.setNsPrefix("dc",    DC );
		m.setNsPrefix("xsd",   XSD);
		m.setNsPrefix("vann",  VANN);
		m.setNsPrefix("foaf",  FOAF);
		m.setNsPrefix("rdfs",  RDFS);
		m.setNsPrefix("gr",    GR  );
		m.setNsPrefix("skos",  SKOS);
		m.setNsPrefix("org",   ORG );
		m.setNsPrefix("price", PRICE );
		m.setNsPrefix("legal", LEGAL );
		m.setNsPrefix("dei",   DEI );
		
		try {
			File outputFile = new File(filePath);
			if (!outputFile.exists()) {
	        	outputFile.createNewFile();        	 
	        }
			
			FileOutputStream out = new FileOutputStream(outputFile);
			m.write(out, lang);
			out.close();
		}
		catch (IOException e) { System.out.println(e.toString()); }
	}
	
	
  public void dumpUSDL(Model model) {	
    // list the statements in the graph
	    StmtIterator iter = model.listStatements();
	     
	    // print out the predicate, subject and object of each statement
	    while (iter.hasNext()) {
	        Statement stmt      = iter.nextStatement();         // get next statement
	        Resource  subject   = stmt.getSubject();   // get the subject
	        Property  predicate = stmt.getPredicate(); // get the predicate
	        RDFNode   object    = stmt.getObject();    // get the object
	        
	        System.out.print(subject.toString());
	        System.out.print(" " + predicate.toString() + " ");
	        if (object instanceof Resource) {
	            System.out.print(object.toString());
	        } else {
	            // object is a literal
	            System.out.print(" \"" + object.toString() + "\"");
	        }
	        System.out.println(" .");
	    }
	}

  public Model prettyFormating(String filePath, String lang) {
  	
      Model model = ModelFactory.createDefaultModel();

      InputStream in = FileManager.get().open( filePath );
      if (in == null) {
          throw new IllegalArgumentException( "File: " + filePath + " not found");
      }
      
      model.read(in, USDL, lang);

		try {
	        OutputStream out = new FileOutputStream(filePath+"9");
	        model.write(out, "TTL");
	        out.close();
		}
		catch (IOException e) { System.out.println(e.toString()); }
       
      return model;
  }
}
