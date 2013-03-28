jQuery ($) ->
  $table = $('.container table')
  $table.append "<thead><tr><th>ID</th><th>Name</th><th>Master</th><th>LastCheck</th><th>DomainType</th><th>NotifiedSerial</th><th>Account</th></tr></thead>"
  changed = []
  domainListUrl = $table.data('list')
  
  loadDomainsTable = ->
    $.get domainListUrl, (domains) ->
      $.each domains, (index, domain) ->
        row = $('<tr/>').append $('<td/>').text (domain.id)
        row.attr 'contenteditable', false
        $table.append row
        loadDomainDetails row
        
  domainDetailsUrl = (id) ->
    $table.data('details').replace '0', id
   
  loadDomainDetails = (tableRow) ->
    id = tableRow.text()
    $.get domainDetailsUrl(id), (domain) ->
      tableRow.append $('<td/>').append("<a contenteditable=\"false\" href=\"/records/list/domain/#{domain.id}\">" + domain.name + '</a>')
      tableRow.append $('<td/>').text(domain.master)
      tableRow.append $('<td/>').text(domain.lastCheck)
      tableRow.append $('<td/>').text(domain.domainType)
      tableRow.append $('<td/>').text(domain.notifiedSerial)
      tableRow.append $('<td/>').text(domain.account)
        
  loadDomainsTable()
   
  saveRow = ($row) ->
    [id, name, master, lastCheck, domainType, notifiedSerial, account] = $row.children().map -> $(this).text()
    domain =
      id: parseInt(id)
      name: name
      master: master
      lastCheck: parseInt(lastCheck)
      domainType: domainType
      notifiedSerial: parseInt(notifiedSerial)
      account: account
    jqxhr = $.ajax
      type: "PUT"
      url: domainDetailsUrl(id)
      contentType: "application/json"
      data: JSON.stringify domain
    jqxhr.done (response) ->
      response = 'saved'
      $label = $('<span/>').addClass('label label-success')
      $row.children().last().append $label.text(response)
      $label.delay(3000).fadeOut()
    jqxhr.fail (data) ->
      $label = $('<span/>').addClass('label label-important')
      message = data.responseText || data.statusText
      $row.children().last().append $label.text(message)
      $label.delay(3000).fadeOut()
      
  $('[contenteditable]').live 'blur', ->
    changed.push(this)
    console.log('contenteditable: edited content', changed)
    
  $("#button").click ->
    console.log('submitting changes ...')
    for row in changed
      saveRow $(row)