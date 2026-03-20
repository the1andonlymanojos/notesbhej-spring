from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"

API = "http://localhost:8080/api/v1"
SPRING_API = "http://localhost:8080/api/v1/courses"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)
# -------------------------
# Fetch Supabase data
# -------------------------

sup_tags = supabase.table("tags").select("*").execute().data
contents = supabase.table("course_contentnew").select("*").execute().data

# -------------------------
# Fetch backend data
# -------------------------

# existing tags (avoid duplicates)
backend_tags = requests.get(f"{API}/tags").json()
tag_name_to_new_id = {t["name"]: t["id"] for t in backend_tags if "name" in t}

# existing content
backend_contents = requests.get(f"{API}/course-content").json()
resource_to_content_id = {
    c["resourceUrl"]: c["id"]
    for c in backend_contents
}

# -------------------------
# Create missing tags
# -------------------------
print(sup_tags)
for tag in sup_tags:
    name = tag["name"]

    if name in tag_name_to_new_id:
        continue

    r = requests.post(f"{API}/tags", json={"name": name})

    if r.status_code != 200:
        print("Failed tag:", name, r.text)

        # 🔥 IMPORTANT: re-fetch after creation
    backend_tags = requests.get(f"{API}/tags").json()
    print(backend_tags)
    tag_name_to_new_id = {t["name"]: t["id"] for t in backend_tags}


# -------------------------
# Build old -> new tag map
# -------------------------

old_tagid_to_newid = {}

for tag in sup_tags:
    old_id = tag["id"]
    name = tag["name"]

    if name in tag_name_to_new_id:
        old_tagid_to_newid[old_id] = tag_name_to_new_id[name]

# -------------------------
# Patch content with tags
# -------------------------

for row in contents:

    resource_url = row.get("resource_url")

    if not resource_url:
        continue

    if resource_url not in resource_to_content_id:
        print("Missing content:", resource_url)
        continue

    new_content_id = resource_to_content_id[resource_url]

    old_tag_ids = row.get("tag_ids") or []

    # handle string ids if needed
    old_tag_ids = [int(x) for x in old_tag_ids]

    new_tag_ids = [
        old_tagid_to_newid.get(tid)
        for tid in old_tag_ids
        if tid in old_tagid_to_newid
    ]

    if not new_tag_ids:
        continue

    payload = {
        "tagIds": new_tag_ids
    }

    r = requests.patch(
        f"{API}/course-content/{new_content_id}",
        json=payload
    )

    print(f"{row['id']} -> {new_content_id} :", r.status_code)