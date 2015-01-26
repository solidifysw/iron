$(function(){
    var Person = Backbone.Model.extend({urlRoot : '/api/person'});
    var myName = new Person({id: "12345"});
    myName.fetch({reset:true});
    myName.on("change", function(msg){
        console.log(myName.get('firstName')+' '+myName.get('lastName'));
    });
    myName.save('firstName','Bob');

    console.log(myName.get('firstName'));
});