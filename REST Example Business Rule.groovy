// requried imports
import wslite.rest.RESTClient;
import wslite.http.auth.HTTPBasicAuthorization;

// define the REST path
def restRequest = "getNewTickets";

// setup REST client
def url = " https://customdev.journaltech.com/api/rest/";
RESTClient restClient = new RESTClient(url);

// add user credentials
restClient.authorization = new HTTPBasicAuthorization("USERNAME", "PASSWORD");

// check for new tickets
def newTicket = restClient.get(path: restRequest);

// check status of response
switch(newTicket.statusCode) {
	case 204:
  		// no new cases at this time
  		logger.info("No new cases at this time.");
  		return;
  		break;
  
  	case 205:
  		// demo complete, reset to continue
  		logger.info("Demo complete! Run again to reset.");
  		return;
  		break;
}

// ensure OK status
if (newTicket.statusCode != 200) {
	logger.error("REST response is incorrect, status = " + restResponse.statusCode);
  	return;
}

// create a new person record
Person newPerson = new Person();
newPerson.firstName = newTicket.json.firstName;
newPerson.middleName = newTicket.json.middleName;
newPerson.lastName = newTicket.json.lastName;
newPerson.saveOrUpdate();

// add the drivers license number to the person record
Identification dlNumber = new Identification();
dlNumber.identificationClass = "STID";  // STID: State Identification
dlNumber.identificationType = "DL";     // DL:   Driver's License Number
dlNumber.identificationNumber = newTicket.json.driversLicenseNumber;
dlNumber.associatedPerson = newPerson;
dlNumber.saveOrUpdate();

// create the traffic newTicket case
Case newCase = new Case();
newCase.caseNumber = newTicket.json.ticketNumber;
newCase.caseType = "TRAFFIC";
newCase.saveOrUpdate();

// add person to the case as a newParty
Party newParty = new Party();
newParty.case = newCase;
newParty.partyType = 'DEF'; // DEF: Defendant
newParty.person = newPerson;
newParty.saveOrUpdate();

// add the newCharge
Charge newCharge = new Charge();
newCharge.associatedParty = newParty;
newCharge.chargeType = "TRAF";  // TRAF: Traffic Citation
newCharge.description = newTicket.json.violationDesc;
newCharge.chargeNumber = newTicket.json.statuteCode;
newCharge.location = newTicket.json.location;
newCharge.saveOrUpdate();


// done!


