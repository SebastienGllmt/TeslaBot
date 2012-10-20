// NOTE
//  Custom scripts for developer.skype.com
//  JS filed moved from CMS - /javascripts/skype/scripts.js to current location
//  All scripts here use JQuery
// ========================================================================*/

$(document).ready(function(){
  
  // React to topics sort dropdown
  $('#forum select#s').change(function(){    
    var param = this.options[this.selectedIndex].value || '',
        view = $('div.by-category').attr('data-view'),
        category = $('div.by-category').attr('data-category');

    location.href = "?view=" + view + "&s=" + param + "&for_category=" + category;
  });

  // FUNCTION TO REPLACE SUBMIT BUTTON WITH GRAPHIC
  var submitButtonReplacement = {
    initialize: function(classname){
      this.allButtons = $('input'+classname);
      this.replaceButtons();
    },
    
    newHTML: function(input) {
      var html;
      if(input.hasClass('user-signup')) {
        html = $('<button tabindex="3" class="button" id="login" type="submit"><span class="blue"><strong>'+ input.attr('value') + '</strong></span></button>')
      } else {
        html = $('<div class="submitButton"><a href="#">' + input.attr('value') + '</a></div>');
      }
      return html;
    },
    
    replaceButtons: function(){
      var that = this;
      this.allButtons.each( function() {        
        var input = $(this);
        that.newHTML(input)
          .attr('id', this.id)
          .insertBefore(this)
          .click( function() {
            if($(this).hasClass('disabled')) {
              return;
            } else {
              $(this).parents('form').submit();
            }
            return false;
          });
        input.hide();
      });
    }
  }

  $(document).ready(function(){
    submitButtonReplacement.initialize('.replaceSubmit');
  });


  // CSS SELECTORS FOR IE
  $("#sidebar .promo:first-child").addClass("first");
  $("ul.checkboxGroup:last-child").addClass("last");

  // DROPDOWN (in header)
    $('#header .dropDown ul').hide().addClass('selected');
  $("#header .dropDown h1").css("cursor", "pointer");
  $("#header .dropDown h1").click(function(){
    $(this).toggleClass("selected");
    $("#header .dropDown ul").slideToggle("400");
    return false;
    });

  // OTHER USERS - ARE YOU SURE show/hide
  $('p.areYouSure').hide();
    $('a.delete').click(function(e) {
    e.preventDefault();
      $(this).parents("p").next("p").slideToggle(400).prev();
    });
    $('a.no').click(function(e) {
    e.preventDefault();
      $(this).parents("p").hide(400).prev();
    });

  // DISABLE SUBMIT BUTTON
    $(document).ready(function() {
      $("#downloadKitButton").attr("disabled", "disabled").addClass("disabled");
      $("#downloadKitButton a").click(function() {return false;});
      $("#downloadKitTerms").click(function() {
            var checked_status = this.checked;
            if (checked_status == true) {
                $("#downloadKitButton").removeAttr("disabled").removeClass("disabled");
    // if the download link is supposed to do more than this please read http://api.jquery.com/unbind/
    $("#downloadKitButton a").click(function() {$(this).parents('form').submit();});
            }
            else {
                $("#downloadKitButton").attr("disabled", "disabled").addClass("disabled");
    // if the download link is supposed to do more than this please read http://api.jquery.com/unbind/
    $("#downloadKitButton a").unbind('click');
            }
        });
    });
});

// Expandable table of contents menu
$(document).ready(function() {

  // highlight section/page
  $(".navigation ul.primaryNav > li > ul.secondaryNav > li > a").filter(function(idx){
    return window.location['pathname'] == $(this).attr('href');
  }).parent().addClass('selected').parent().parent().addClass("selected");

  $(".navigation ul.primaryNav > li[class!=selected]").find("ul").slideToggle("medium");
  
  $(".navigation ul.primaryNav > li > a").click(function() {
    $(".navigation ul.primaryNav > li.selected").removeClass('selected').find("ul").hide();
    $(this).parent().toggleClass("selected").toggleClass("").find("ul").slideToggle("medium");
    return false;
  });
});

// Signup form confirmation step
/*
$(document).ready(function() {
  var form                = $('#signup_form');
  var confirmationPane    = $("#signup_confirmation");
  var confirmationTrigger = $("#signup_confirmation_trigger");

  if (form.length == 0) {
    return;
  }

  var extractLabel = function(element) {
    return $(element).find("label").first().contents()
                     .filter(function() { return this.nodeType == 3; })
                     .map(function() { return this.nodeValue; })
                     .get().join('');
  }

  var populateConfirmationTable = function() {
    var table = confirmationPane.find("table");
    table.empty();

    var inputs = $("#signup_user_attributes_email_input");

    inputs.each(function() {
      table.append(
        $("<tr></tr>").append($("<th></th>").text(extractLabel(this)))
                      .append($("<td></td>").text($(this).find("input").val())));
    });
  }

  $("#signup_confirmation_trigger").fancybox({
    onStart:    populateConfirmationTable,
    onComplete: function() { form.data("confirming", true); },
    onClosed:   function() { form.data("confirming", false); }
  });

  $("#signup_cancel").click(function(event) {
    $.fancybox.close();
    event.preventDefault();
  });

  $("#signup_confirm").click(function(event) {
    form.submit()
    event.preventDefault();
  });

  form.submit(function(event) {
    if (form.data("confirming")) {
      return;
    }

    if (form.valid()) {
      confirmationTrigger.click();
    }

    event.preventDefault();
  });
});
*/

$(document).ready(function() {


  // signup form dynamic fields
  var companySize       = $("#signup_profile_attributes_company_size");
  var companyType       = $("#signup_profile_attributes_company_type");
  var companyCustomType = $("#signup_profile_attributes_custom_company_type");
  var companyContainer  = $("#company_profile_fields_container");

  companySize.change(function() {
    if (this.selectedIndex > 1) {
      companyContainer.show();
    } else {
      companyContainer.hide();
    }
  });


  // Searchfield - hide "search" on focus
  $("#skypeSearchForm :input").focus(function(){
      if($(this).val() == "Search") {
        $(this).val('')
      }
  });

  $("#skypeSearchForm :input").blur(function(){
      if($(this).val() == "") {
        $(this).val("Search")
      }
  });

  //add vertical space to multiple buttons on Tools page
  $("div.toolsContentHolder ul.ctaHolder li a").css('margin-bottom', '15px')


});
