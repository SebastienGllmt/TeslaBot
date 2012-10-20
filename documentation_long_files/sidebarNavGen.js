function createPrimaryNavMenu(menu){
  var primaryNav = $('.primaryNav');
  
  

  if (primaryNav == null || primaryNav.length == 0)
  //if(primaryNav == null)
  {
    return;
  }
  
  var generateMenuItem = function(menuItem)
  {
    var menuItemHtml = '';
    
    menuItemHtml += '<li id="' + menuItem.id + '"><a href="' + menuItem.href + '"><span>' + menuItem.text + '</span></a>';
    
    if (menuItem.subs != null && menuItem.subs != 'undefined' && menuItem.subs.length > 0)
    {
      menuItemHtml += '<ul class="secondaryNav">';
      
      var subs = menuItem.subs;
      for (var i = 0, max = subs.length; i < max; i++)
      {
        menuItemHtml += generateSubMenuItem(subs[i]);
      }
      
      menuItemHtml += '</ul>';
    }
    
    menuItemHtml += '</li>';
    
    return menuItemHtml;
  };
  
  var generateSubMenuItem = function(menuItem)
  {
    return '<li id="' + menuItem.id + '"><a href="' + menuItem.href + '" title="' + menuItem.title + '">' + menuItem.text + '</a></li>';
  };
  
  var generatedMenu = '';
  
  for (var i = 0, max = menu.length; i < max; i++)
  {
    generatedMenu += generateMenuItem(menu[i]);
  }

//  primaryNav.innerHTML = generatedMenu;
	primaryNav.html(generatedMenu);
}


//alert(navMenu);

$(function() {
	if(typeof navMenu != 'undefined')
	{
		createPrimaryNavMenu(navMenu);
	}	
});
