import os
import glob

template_dir = r"c:/Users/MR KHANG/Downloads/HUTECH_BUS-ANKHANG/HUTECH_BUS-ANKHANG/src/main/resources/templates"
files = glob.glob(os.path.join(template_dir, "*.html"))

target_menu_old = """        <div id="admin-submenu" style="display: none; padding-bottom: 5px;">
            <a href="#" onclick="showAdminCreateTicket(); return false;" class="nav-link" style="background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid #10b981; margin-left: 15px; padding: 8px 15px; font-size: 0.9rem; margin-bottom: 5px;">&#x21B3; Tạo vé & Quản lý</a>
            <a href="/dashboard" onclick="showAdminPanel();" id="admin-sidebar-link" class="nav-link" style="display: none; background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid #ef4444; margin-left: 15px; padding: 8px 15px; font-size: 0.9rem; margin-bottom: 8px;">&#x21B3; Phê duyệt vé</a>
        </div>"""

target_menu_new = """        <div id="admin-submenu" style="display: none; padding-bottom: 5px;">
            <a href="/dashboard" onclick="showAdminPanel();" class="nav-link" style="background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid #ef4444; margin-left: 15px; padding: 8px 15px; font-size: 0.9rem; margin-bottom: 8px;">&#x21B3; Phê duyệt vé</a>
        </div>"""

js_old = "adminSidebarLink.style.display = 'flex';"
js_new = "adminSidebarLink.style.display = 'block';"

for filepath in files:
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    
    modified = False
    
    if target_menu_old in content:
        content = content.replace(target_menu_old, target_menu_new)
        modified = True
        
    if js_old in content:
        content = content.replace(js_old, js_new)
        modified = True
        
    if modified:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {os.path.basename(filepath)}")

print("Done")
