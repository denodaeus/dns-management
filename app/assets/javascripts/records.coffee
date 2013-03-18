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
       tableRow.append $('<td/>').text(record.recordType)
       tableRow.append $('<td/>').text(record.content)
       tableRow.append $('<td/>').text(record.ttl)
       tableRow.append $('<td/>').text(record.priority)
       tableRow.append $('<td/>').text(record.changeDate)
        
   loadRecordsTable()