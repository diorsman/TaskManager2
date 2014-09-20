
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
/*Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
  });*/
var _ = require("underscore");
Parse.Cloud.beforeSave("Project", function(request, response) {

  var project = request.object;

  var toLowerCase = function(w) {return w.toLowerCase();};

  var projectName = project.get("Name").split(/\s+/);
  projectName = _.map(projectName, toLowerCase);

  var projectDescription = project.get("Description").split(/\s+/);
  projectDescription = _.map(projectDescription, toLowerCase);
  var stopWords = ["the", "in", "and"];
  projectDescription = _.filter(projectDescription, function(w) {return w.match(/^\w+$/) && !_.contains(stopWords, w);});

  var projectAdmin = project.get("AdminName").split(/\s+/);
  projectAdmin = _.map(projectAdmin, toLowerCase);

  var projectColor = project.get("Color");
  projectColor = projectColor.toLowerCase();
  projectColor = [projectColor];

  var projectUserSearch = [];
  var projectUser = project.get("UserName");
  projectUser = _.map(projectUser, toLowerCase);
  for (var i = 0; i < projectUser.length; i++) {
    var name = projectUser[i];
    name = name.split(/\s+/);
    projectUserSearch = projectUserSearch.concat(name);
  }

  project.set("NameSearch", projectName);
  project.set("DescriptionSearch", projectDescription);
  project.set("AdminSearch", projectAdmin);
  project.set("ColorSearch", projectColor);
  project.set("UserSearch", projectUserSearch);
  response.success();
});

Parse.Cloud.afterSave("Project", function(request) {

  var project = request.object;

  if (project.existed()) {

    var chatClass = Parse.Object.extend("Chat");
    var query = new Parse.Query(chatClass);
    query.equalTo("project", project);
    query.find({
      success: function(results) {
        var chat = results[0];

        var projectUserName = project.get("UserName");
        var projectUserId = project.get("UserId");

        chat.set("UserName", projectUserName);
        chat.set("UserId", projectUserId);

        var projectAdminId = project.get("Administrator").id;
        var projectAdminName = project.get("AdminName");
        chat.addUnique("UserName", projectAdminName);
        chat.addUnique("UserId", projectAdminId);
        chat.save();
      }
    });

    return;
  }

  var projectAdminId = project.get("Administrator").id;
  var projectAdminName = project.get("AdminName");
  var projectUserName = project.get("UserName");
  var projectUserId = project.get("UserId");

  var ChatClass = Parse.Object.extend("Chat");
  var chat = new ChatClass();

  chat.set("project", request.object);
  chat.set("UserName", projectUserName);
  chat.set("UserId", projectUserId);
  chat.addUnique("UserName", projectAdminName);
  chat.addUnique("UserId", projectAdminId);
  chat.save();
});
