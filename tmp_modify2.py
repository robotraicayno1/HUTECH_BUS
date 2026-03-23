import os, glob, re

folders = [
    r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\src\main\resources\templates",
    r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\target\classes\templates"
]

old_block = '''<div style="background: rgba(0,0,0,0.15); border-radius: 12px; padding: 5px 5px 0 5px; margin-bottom: 8px;">
        <a href="/ticket-types" class="nav-link" style="margin-bottom: 5px;">Quản lý loại vé</a>
        <a href="/dashboard" id="admin-sidebar-link" class="nav-link" style="display: none; background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid #ef4444; margin-left: 15px; padding: 8px 15px; font-size: 0.9rem; margin-bottom: 5px;">&#x21B3; Phê duyệt vé</a>
    </div>'''

new_block = '''<div style="background: rgba(0,0,0,0.15); border-radius: 12px; padding: 5px 5px 0 5px; margin-bottom: 8px;">
        <div class="nav-link" style="margin-bottom: 5px; cursor: pointer; display: flex; justify-content: space-between; align-items: center;" onclick="const menu = this.nextElementSibling; menu.style.display = menu.style.display === 'none' ? 'block' : 'none'; this.querySelector('span').style.transform = menu.style.display === 'none' ? 'rotate(0deg)' : 'rotate(180deg)';">
            Quản lý loại vé
            <span style="font-size: 0.8rem; transition: transform 0.3s; display: inline-block;">▼</span>
        </div>
        <div id="admin-submenu" style="display: none; padding-bottom: 5px;">
            <a href="/dashboard" id="admin-sidebar-link" class="nav-link" style="display: none; background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid #ef4444; margin-left: 15px; padding: 8px 15px; font-size: 0.9rem; margin-bottom: 0;">&#x21B3; Phê duyệt vé</a>
        </div>
    </div>'''

for directory in folders:
    for filepath in glob.glob(os.path.join(directory, '*.html')):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Modify the content
        content = content.replace(old_block, new_block)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

print("Done")
