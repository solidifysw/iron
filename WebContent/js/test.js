$(function(){
    var Person = Backbone.Model.extend({urlRoot : '/api/person'});
    var myName = new Person({id: "12345"});
    myName.fetch({reset:true});
    myName.on("change", function(msg){
        console.log(myName.attributes);
    })
});