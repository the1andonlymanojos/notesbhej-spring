from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"
API = "http://localhost:8080/api/v1"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)


# -------------------------
# Pull Spring data
# -------------------------

courses = requests.get(f"{API}/courses").json()
profs = requests.get(f"{API}/professors").json()

course_map = {c["title"]: c["id"] for c in courses}
prof_map = {p["name"]: p["id"] for p in profs}


# -------------------------
# Pull Supabase data
# -------------------------

sup_courses = supabase.table("coursenew").select("*").execute().data
sup_profs = supabase.table("professorsnew").select("*").execute().data
contents = supabase.table("course_contentnew").select("*").execute().data


# Build old → new ID maps

old_course_map = {}
for c in sup_courses:
    code = c["title"]
    if code in course_map:
        old_course_map[c["id"]] = course_map[code]

old_prof_map = {}
for p in sup_profs:
    email = p["name"]
    if email in prof_map:
        old_prof_map[p["id"]] = prof_map[email]


# -------------------------
# Visibility mapping
# -------------------------

def visibility(row):
    if row.get("deleted"):
        return "DELETED"
    if row.get("visible"):
        return "VISIBLE"
    return "PENDING_REVIEW"


# -------------------------
# Insert content
# -------------------------

for row in contents:

    payload = {
        "title": row.get("title"),
        "resourceUrl": row.get("resource_url"),
        "r2Url": row.get("r2_url"),
        "year": row.get("year"),
        "batch": row.get("batch"),
        "semesterNumber": row.get("semester_number"),
        "fileType": row.get("filetype"),
        "createdAt": row.get("created_at"),
        "visibility": visibility(row),

        "uploadedBy": {"id": 1}
    }

    if row.get("course_id") in old_course_map:
        payload["course"] = {"id": old_course_map[row["course_id"]]}

    if row.get("professor_id") in old_prof_map:
        payload["professor"] = {"id": old_prof_map[row["professor_id"]]}

    r = requests.post(f"{API}/course-content", json=payload)

    print(row["id"], r.status_code)