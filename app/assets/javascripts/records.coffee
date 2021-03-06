jQuery ($) ->
  $table = $('.container table')
  $table.append "<thead><tr><th>ID</th><th>Name</th><th>DomainID</th><th>RecordType</th><th>Content</th><th>TTL</th><th>Priority</th><th>ChangeDate</th><th>AccountId</th></tr></thead>"
  changed = []
  recordListUrl = $table.data('list')
  
  loadRecordsTable = ->
    $.get recordListUrl, (records) ->
      $.each records, (index, record) ->
        row = $('<tr/>').append $('<td/>').text (record.id)
        row.attr 'contenteditable', true
        $table.append row
        loadRecordDetails row
        
  recordDetailsUrl = (id) ->
    $table.data('details').replace '0', id
   
  loadRecordDetails = (tableRow) ->
    id = tableRow.text()
    $.get recordDetailsUrl(id), (record) ->
      tableRow.append $('<td/>').text(record.name)
      tableRow.append $('<td/>').text(record.domainId)
      tableRow.append $('<td/>').text(record.recordType)
      tableRow.append $('<td/>').text(record.content)
      tableRow.append $('<td/>').text(record.ttl)
      tableRow.append $('<td/>').text(record.priority)
      tableRow.append $('<td/>').text(record.changeDate)
      tableRow.append $('<td/>').text(record.accountId)
        
  loadRecordsTable()
   
  saveRow = ($row) ->
    [id, name, domainId, recordType, content, ttl, priority, changeDate] = $row.children().map -> $(this).text()
    record =
      id: parseInt(id)
      name: name
      domainId: parseInt(domainId)
      recordType: recordType
      content: content
      ttl: parseInt(ttl)
      priority: parseInt(priority)
      changeDate: parseInt(changeDate)
      accountId: accountId
    jqxhr = $.ajax
      type: "PUT"
      url: recordDetailsUrl(id)
      contentType: "application/json"
      data: JSON.stringify record
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
  	
   