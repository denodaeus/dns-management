jQuery ($) ->
  $table = $('.container table')
  $table.append "<thead><tr><th>ID</th><th>AccountId</th><th>RecordsCount</th></tr></thead>"
  changed = []
  accountsListUrl = $table.data('list')
  
  loadAccountsTable = ->
    $.get accountListUrl, (accounts) ->
      $.each accounts, (index, account) ->
        row = $('<tr/>').append $('<td/>').text (account.id)
        row.attr 'contenteditable', true
        $table.append row
        loadRecordDetails row
        
  accountDetailsUrl = (id) ->
    $table.data('details').replace '0', id
   
  loadAccountDetails = (tableRow) ->
    id = tableRow.text()
    $.get accountDetailsUrl(id), (account) ->
      tableRow.append $('<td/>').text(account.id)
      tableRow.append $('<td/>').text(account.accountId)
      tableRow.append $('<td/>').text(0)
        
  loadAccountsTable()