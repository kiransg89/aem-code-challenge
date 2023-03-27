(function(window, document, $) {
    "use strict";
    
    var maxAge, minAge;    
    
    function getMinMax(){
		$.ajax({
            // add the servlet path
            url: "/etc/age.json",
            method: "GET",
            async: true,
            cache: false,
            contentType: false,
            processData: false,
         }).done(function (data) {
			 if(data){
				 maxAge = parseInt(data.maxAge);
				 minAge = parseInt(data.minAge);
                 console.log( "Min Age is ", minAge);
		    	 console.log( "Max Age is ", maxAge);
			 }
		 });
	}
	
	$( document ).ready(function() {
		getMinMax();
    	console.log( "ready!" );
	});
	
	$(document).off("click", "#cmp-submit").on("click", "#cmp-submit", function (event) {
		var age = parseInt($("#cmp-age").val());
		if(age < minAge || age > maxAge){
		    alert("Please enter the age between " + minAge + " Max age " + maxAge);
            event.preventDefault();
		}
	});
		
    
})(window, document, $);