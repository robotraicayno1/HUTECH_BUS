import os, glob, re

folders = [
    r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\src\main\resources\templates",
    r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\target\classes\templates"
]

js_code = '''
        function showAdminCreateTicket() {
            document.getElementById('admin-panel-container').style.display = 'none';
            document.getElementById('admin-create-ticket-container').style.display = 'block';
        }
        
        function showAdminPanel() {
            document.getElementById('admin-create-ticket-container').style.display = 'none';
            document.getElementById('admin-panel-container').style.display = 'block';
            if(typeof loadPendingTickets === 'function') loadPendingTickets();
        }
'''

for directory in folders:
    index_path = os.path.join(directory, 'index.html')
    if os.path.exists(index_path):
        with open(index_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        if 'function showAdminCreateTicket' not in content:
            pos = content.rfind('</script>')
            if pos != -1:
                content = content[:pos] + js_code + content[pos:]
            
            with open(index_path, 'w', encoding='utf-8') as f:
                f.write(content)

print("JS appended")
