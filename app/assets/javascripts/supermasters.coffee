jQuery ($) ->
  $table = $('.container table')
  $table.append "<thead><tr><th>ID</th><th>IP</th><th>Nameserver</th><th>Account</th></tr></thead>"
  supermasterListUrl = $table.data('list')
  
  loadSupermastersTable = ->
    $.get supermasterListUrl, (supermasters) ->
      $.each supermasters, (index, supermaster) ->
        row = $('<tr/>').append $('<td/>').text (supermaster.id)
        row.attr 'contenteditable', true
        $table.append row
        loadSupermasterDetails row
        
  supermasterDetailsUrl = (id) ->
    $table.data('details').replace '0', id
   
  loadSupermasterDetails = (tableRow) ->
    id = tableRow.text()
    $.get supermasterDetailsUrl(id), (supermaster) ->
      tableRow.append $('<td/>').text(supermaster.ip)
      tableRow.append $('<td/>').text(supermaster.nameServer)
      tableRow.append $('<td/>').text(supermaster.account)
        
  loadSupermastersTable()
   
  saveRow = ($row) ->
    [id, ip, nameServer, account] = $row.children().map -> $(this).text()
    supermaster =
      id: parseInt(id)
      ip: ip
      nameServer: nameServer
      account: account
    jqxhr = $.ajax
      type: "PUT"
      url: supermasterDetailsUrl(id)
      contentType: "application/json"
      data: JSON.stringify supermaster
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
    saveRow $(this)