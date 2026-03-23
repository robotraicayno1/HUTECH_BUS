import os, glob

base_dir = r"c:\Users\MR KHANG\Downloads\HUTECH_BUS-ANKHANG\HUTECH_BUS-ANKHANG\src\main\resources\templates"
index_file = os.path.join(base_dir, "index.html")

with open(index_file, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Create approve-tickets.html
approve_content = content.replace("HUTECHBUS - Dashboard", "HUTECHBUS - Phê duyệt vé")
# Make dashboard inactive
approve_content = approve_content.replace('href="/dashboard" class="nav-link active"', 'href="/dashboard" class="nav-link"')

# Change the sidebar link in approve-tickets.html to be active
approve_content = approve_content.replace(
    'href="/dashboard" onclick="showAdminPanel();"',
    'href="/approve-tickets"'
)
approve_content = approve_content.replace(
    '<a href="/approve-tickets" class="nav-link" style="background: rgba(255,255,255,0.1); color: #fff; border-left: 3px solid #ef4444;',
    '<a href="/approve-tickets" class="nav-link active" style="background: rgba(255,107,0,1); color: #fff; border-left: 3px solid #fff;'
)

# Remove user stats grid
start_stats = approve_content.find('<div class="stats-grid">')
if start_stats != -1:
    end_stats = approve_content.find('<div class="dashboard-layout">', start_stats)
    approve_content = approve_content[:start_stats] + approve_content[end_stats:]

# Remove user journeys
start_journeys = approve_content.find('<div id="user-journeys-container"')
if start_journeys != -1:
    end_journeys = approve_content.find('<div id="user-qr-container"', start_journeys)
    approve_content = approve_content[:start_journeys] + approve_content[end_journeys:]

# Remove QR container
start_qr = approve_content.find('<div id="user-qr-container"')
if start_qr != -1:
    end_qr = approve_content.find('<!-- BẢNG ADMIN QUẢN LÝ PHÊ DUYỆT -->', start_qr)
    approve_content = approve_content[:start_qr] + approve_content[end_qr:]

# Make admin panel visible by default
approve_content = approve_content.replace('id="admin-panel-container" class="content-card" style="display: none; grid-column: 1 / -1; margin-top: 20px;"', 'id="admin-panel-container" class="content-card" style="display: block; grid-column: 1 / -1; margin-top: 20px;"')

with open(os.path.join(base_dir, "approve-tickets.html"), "w", encoding="utf-8") as f:
    f.write(approve_content)

# 2. Modify index.html to REMOVE admin panel completely
index_content = content
start_admin_panel = index_content.find('<!-- BẢNG ADMIN QUẢN LÝ PHÊ DUYỆT -->')
if start_admin_panel != -1:
    end_admin_panel = index_content.find('</div>\n    </div>\n\n    <script th:inline="javascript">', start_admin_panel)
    if end_admin_panel != -1:
        index_content = index_content[:start_admin_panel] + index_content[end_admin_panel:]

# Remove JS logic for Admin panel from index.html to prevent null errors
start_admin_js = index_content.find('// Code kiểm tra vai trò để ẩn/hiện logic Admin')
if start_admin_js != -1:
    end_admin_js = index_content.find('</script>', start_admin_js)
    if end_admin_js != -1:
        index_content = index_content[:start_admin_js] + "\n        " + index_content[end_admin_js:]

# 3. Update all templates to use /approve-tickets instead of onclick showAdminPanel
for file_path in glob.glob(os.path.join(base_dir, "*.html")):
    if file_path.endswith("approve-tickets.html"):
        continue
    
    if file_path == index_file:
         file_content = index_content
    else:
        with open(file_path, "r", encoding="utf-8") as f:
            file_content = f.read()

    # Link replacement
    file_content = file_content.replace(
        'href="/dashboard" onclick="showAdminPanel();"',
        'href="/approve-tickets"'
    )
    
    with open(file_path, "w", encoding="utf-8") as f:
         f.write(file_content)

print("Done generating approve-tickets and updating links.")
