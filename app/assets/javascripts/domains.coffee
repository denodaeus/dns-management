jQuery ($) ->
  $table = $('.container table')
  domainListUrl = $table.data('list')
  
  loadDomainsTable = ->
    $.get domainListUrl, (domains) ->
      $.each domains, (index, domain) ->
        row = $('<tr/>').append $('<td/>').text (domain.id)
        row.attr 'contenteditable', true
        $table.append row
        loadDomainDetails row
        
  domainDetailsUrl = (id) ->
    $table.data('details').replace '0', id
   
  loadDomainDetails = (tableRow) ->
    id = tableRow.text()
    $.get domainDetailsUrl(id), (domain) ->
      tableRow.append $('<td/>').text(domain.name)
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
      $label = $('<span/>').addClass('label label-success')
      $row.children().last().append $label.text('success')
      $label.delay(3000).fadeOut()
    jqxhr.fail (data) ->
      $label = $('<span/>').addClass('label label-important')
      message = data.responseText || data.statusText
      $row.children().last().append $label.text(message)
      
  $('[contenteditable]').live 'blur', ->
    saveRow $(this)