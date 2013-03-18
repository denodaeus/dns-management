jQuery ($) ->
  $table = $('.container table')
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
    jqxhr = $.ajax
      type: "PUT"
      url: recordDetailsUrl(id)
      contentType: "application/json"
      data: JSON.stringify record
    jqxhr.done (response) ->
      $label = $('<span/>').addClass('label label-success')
      $row.children().last().append $label.text('success')
      $label.delay(3000).fadeOut()
    jqxhr.fail (data) ->
      $label = $('<span/>').addClass('label label-important')
      message = data.responseText || data.statusText
      $row.children().last().append $label.text(message)
      $label.delay(3000).fadeOut()
      
  $('[contenteditable]').live 'blur', ->
  	saveRow $(this)
   