package com.egen.user.management.API;

import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;

import org.bson.types.ObjectId;

/**
 * Created by Dhivya.S on 05/10/2016
 *
 */
public class AppController {
	private Configuration config;
	private Template template;
	private DBCollection coll;

	public static void main(String[] args) {

		try {
			AppController controller = new AppController();
			controller.initialise();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public AppController() throws UnknownHostException {
		Spark.setPort(4571);
		config = new Configuration();
		config.setClassForTemplateLoading(AppController.class, "/");
		coll = new MongoClient(new MongoClientURI("mongodb://localhost"))
				.getDB("UserDetails").getCollection("users");

	}

	private void initialise() throws Exception {
		catalogueRoute("/", "catalogueTemplate.fmt");
		userRoute("/user", "studentTemplate.fmt");
		userAddRoute("/user/add");
		userUpdateRoute("/user/update/:user_id");
	}

	/*private int getNextID() {
		
		 * Aggregation example
		 * AggregationOutput out = coll.aggregate(new BasicDBObject("$group",
		 * new BasicDBObject("_id", "") 
		 * .append("max_id", new BasicDBObject("$max", "$id")))); 
		 * id = out.results().iterator().next().get("max_id").toString();
		 
		
		Ideally you can define a function in database to return nextID
		// But since this is not a MongoDB demo, I will go with the following.
		
		DBCursor cursor = coll.find();
		return cursor.hasNext() ? Integer.parseInt(cursor.next().get("id").toString())+1 : 1;
	}
   */

	//List of available Spark Java REST APIs - URL http://localhost:4571/ 
	private void catalogueRoute(String routePath, String templateName)
			throws Exception {

		template = config.getTemplate(templateName);
		final StringWriter writer = new StringWriter();

		Map<String, String> catalogueMap = new HashMap<String, String>();
		catalogueMap.put("get_path", "http://localhost/user");
		catalogueMap.put("post_path", "http://localhost/user/add");
		catalogueMap.put("put_path", "http://localhost/user/update/:user_id");
		//catalogueMap.put("delete_path", "http://localhost/user/remove/:user_id");

		template.process(catalogueMap, writer);

		Spark.get(new Route(routePath) {
			@Override
			public Object handle(Request arg0, Response arg1) {
				return writer;
			}

		});

	}

	//Read (GET) All User Details from database, URL http://localhost:4571/user
	private void userRoute(String routePath, final String templateName)
			throws Exception {
		template = config.getTemplate(templateName);
		

		Spark.get(new Route(routePath) {
			StringWriter writers;
			@Override
			public Object handle(Request arg0, Response arg1) {
				
				try {
					List<DBObject> users;
					DBCursor cursor = coll.find();
					try {
						users = cursor.toArray();
					} finally {
						cursor.close();
					}

					SimpleHash hash = new SimpleHash();
					hash.put("users", users);
					writers = new StringWriter();
					template.process(hash, writers);
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return writers;
				
			}

		});

	}

	/*
	 * You can directly run POST using Postman REST Client
	 * Support for GET is added to use HTML form on browser
	 * ADD (POST) data, URL http://localhost:4571/student/add
	 */
	
	private void userAddRoute(final String routePath) throws Exception {

		Spark.get(new Route(routePath) {
			@Override
			public Object handle(Request request, Response response) {
				
					StringBuilder form = new StringBuilder();
				

				form.append(
						"<form id='user-form' method='POST' action='"
								+ routePath + "'>")
						/*.append("User ID: <input type='text' name='id' />")
						.append("<br/>")*/
						.append("User First Name:          <input type='text' name='firstName' />")
						.append("<br/>")
						.append("User Last Name:           <input type='text' name='lastName' />")
						.append("<br/>")
						.append("User Email:               <input type='text' name='email' />")
						.append("<br/>")
						.append("Street Address:           <input type='text' name='street' />")
						.append("<br/>")
						.append("City:                     <input type='text' name='city' />")
						.append("<br/>")
						.append("ZipCode:                  <input type='text' name='zip' />")
						.append("<br/>")
						.append("State:                    <input type='text' name='state' />")
						.append("<br/>")
						.append("Country:                  <input type='text' name='country' />")
						.append("<br/>")
						.append("Company Name:             <input type='text' name='companyName' />")
						.append("<br/>")
						.append("Company Website:          <input type='text' name='companyWebsite' />")
						.append("<br/>")
						.append("User Profile Picture Link: <input type='text' name='user_profilePic' />")
						.append("<br/>")
						.append("</form>")
						.append("<input type='submit' value='Add User' form='user-form' />");

				return form.toString();
			}
		});

		Spark.post(new Route(routePath) {
			
			
			@Override
			public Object handle(Request request, Response response) {
				//String _id = request.queryParams("user_id");
				String _firstName = request.queryParams("firstName");
				String _lastName = request.queryParams("lastName");
				String _email = request.queryParams("email");
				String _streetAddress = request.queryParams("street");
				String _city = request.queryParams("city");
				String _zip = request.queryParams("zip");
				String _state = request.queryParams("state");
				String _country = request.queryParams("country");
				String _companyName = request.queryParams("companyName");
				String _companyWebsite = request.queryParams("companyWebsite");
				String _profilePic = request.queryParams("user_profilePic");
				/*Since MongoDB auto-generates a unique Obbject_Id for every user we enter into the database, 
				 * I have used checking of an existing user with respect to their first name
				 * This can be changed to any other document of the collection 'users'*/
				DBCursor cursor = coll.find(new BasicDBObject("firstName", _firstName));
				if(cursor.hasNext()){
					//response.status(404);
					Spark.get(new Route("/404") {
						   @Override
						   public Object handle(Request request, Response response) {
						       response.type("text/html");
						       return "Error: User Already Exists";
						   }
						});
					response.redirect("/404");
					
					System.out.println("User Already Exists");//When the user already exists in DB, this message is printed in the console.
				}else{
				
				coll.insert(new BasicDBObject("firstName", _firstName)
						.append("lastName", _lastName).append("email", _email)
						.append("street", _streetAddress).append("city", _city)
						.append("zip", _zip).append("state", _state)
						.append("country", _country).append("companyName", _companyName)
						.append("companyWebsite", _companyWebsite).append("profilePic", _profilePic)
						);
				response.status(201);
				response.redirect("/user");
				
				//return "";
				}
				return " ";
			    }
			
			});
	
	}

	
	/*
	 * You can directly run POST using Postman REST Client
	 * Support for GET is added to use HTML form on browser
	 * Update (POST) data, example URL http://localhost/student/update/{Object _id from MongoDB}
	 */		
	
	private void userUpdateRoute(final String routePath) throws Exception{
		
		Spark.get(new Route(routePath) {
			@Override
			public Object handle(Request request, Response response) {
				
				DBObject user = coll.findOne(new BasicDBObject("_id", new ObjectId(request.params(":user_id"))));
				if((user.get("_id").toString()) == null){
					//response.status(404);
					Spark.get(new Route("/404") {
						   @Override
						   public Object handle(Request request, Response response) {
						       response.type("text/html");
						       return "Error: User Not Found";
						   }
						});
					response.redirect("/404");
					
					System.out.println("User Already Exists");//When the user already exists in DB, this message is printed in the console.
				}else{
				
				StringBuilder form = new StringBuilder();

				form.append(
						"<form id='user-form' method='POST' action='"
								+ routePath + "'>")
						.append("User ID: <input type='text' name='user_id' value = '"
								+ user.get("_id").toString() + "' readonly/>")
						.append("<br/>")
						.append("User First Name: <input type='text' name='firstName'  value = '"
								+ user.get("firstName").toString() + "' />")
						.append("<br/>")
						.append("User Last Name: <input type='text' name='lastName' value = '"
								+ user.get("lastName").toString() + "' />")
						.append("<br/>")
						.append("User Email: <input type='text' name='email' value = '"
								+ user.get("email").toString() + "' />")
						.append("<br/>")
						.append("Street Address: <input type='text' name='street' value = '"
								+ user.get("street").toString() + "' />")
						.append("<br/>")
						.append("City: <input type='text' name='city' value = '"
								+ user.get("city").toString() + "' />")
						.append("<br/>")
						.append("ZipCode: <input type='text' name='zip' value = '"
								+ user.get("zip").toString() + "' />")
						.append("<br/>")
						.append("State: <input type='text' name='state' value = '"
								+ user.get("state").toString() + "' />")
						.append("<br/>")
						.append("Country: <input type='text' name='country' value = '"
								+ user.get("country").toString() + "' />")
						.append("<br/>")
						
						.append("<br/>")
						.append("Company Name: <input type='text' name='companyName' value = '"
								+ user.get("companyName").toString() + "' />")
						.append("<br/>")
						.append("Company Website: <input type='text' name='companyWebsite' value = '"
								+ user.get("companyWebsite").toString() + "' />")
						.append("<br/>")
						.append("User Profile Picture Link: <input type='text' name='user_profilePic' value = '"
								+ user.get("profilePic").toString() + "' />")
						.append("<br/>")
						.append("</form>")
						.append("<input type='submit' value='Add User' form='user-form' />");

				return form.toString();
                
			}
				return " ";
			}
			
		});
		
		Spark.post(new Route(routePath) {

			@Override
			public Object handle(Request request, Response response) {
				String _id = request.queryParams("user_id");
				String _firstName = request.queryParams("firstName");
				String _lastName = request.queryParams("lastName");
				String _email = request.queryParams("email");
				String _streetAddress = request.queryParams("street");
				String _city = request.queryParams("city");
				String _zip = request.queryParams("zip");
				String _state = request.queryParams("state");
				String _country = request.queryParams("country");
				String _companyName = request.queryParams("companyName");
				String _companyWebsite = request.queryParams("companyWebsite");
				String _profilePic = request.queryParams("user_profilePic");
				DBCursor cursor = coll.find(new BasicDBObject("_id", new ObjectId(request.params(":user_id"))));
				if(cursor.hasNext()== false){
					//response.status(404);
					Spark.get(new Route("/401") {
						   @Override
						   public Object handle(Request request, Response response) {
						       response.type("text/html");
						       return "Error: User Already Exists";
						   }
						});
					response.redirect("/401");
					
					System.out.println("User Already Exists");//When the user already exists in DB, this message is printed in the console.
				}else{
				coll.update(new BasicDBObject("_id", new ObjectId(_id)), new BasicDBObject("$set", 
						new BasicDBObject("firstName", _firstName)
						.append("lastName", _lastName).append("email", _email)
						.append("street", _streetAddress).append("city", _city)
						.append("zip", _zip).append("state", _state)
						.append("country", _country).append("companyName", _companyName)
						.append("companyWebsite", _companyWebsite).append("profilePic", _profilePic)
						));
				response.status(201);
				response.redirect("/user");
				
				//return "";
				}
				return " ";
			    }
			
			});
		
		
	}

}
