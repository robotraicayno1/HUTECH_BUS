import os, glob, re

folders = [
    r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\src\main\resources\templates",
    r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\target\classes\templates"
]

sub_items = [
    ("Tạo vé cho chuyến", "#10b981", "create-t", "#"),
    ("Vé lượt", "#3b82f6", "ticket-luot", "#"),
    ("Vé ngày", "#3b82f6", "ticket-ngay", "#"),
    ("Vé tháng", "#3b82f6", "ticket-thang", "#"),
    ("Sửa vé", "#f59e0b", "edit-ticket", "#"),
    ("Phê duyệt vé", "#ef4444", "admin-sidebar-link-child", "/dashboard")
]

new_submenu_inner = ""
for item in sub_items:
    name = item[0]
    color = item[1]
    id_attr = f'id="{item[2]}" ' if len(item) > 2 else ''
    href = item[3] if len(item) > 3 else '#'
    
    new_submenu_inner += f'            <a href="{href}" {id_attr}class="nav-link" style="background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid {color}; margin-left: 15px; padding: 8px 15px; font-size: 0.9rem; margin-bottom: 5px;">&#x21B3; {name}</a>\n'

replacement = f'<div id="admin-submenu" style="display: none; padding-bottom: 5px;">\n{new_submenu_inner}        </div>'

for directory in folders:
    for filepath in glob.glob(os.path.join(directory, '*.html')):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # Regex to target <div id="admin-submenu" ...> ... </div>
        content = re.sub(r'<div id="admin-submenu" style="display: none; padding-bottom: 5px;">.*?</div>', replacement, content, flags=re.DOTALL)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

print("Done")
