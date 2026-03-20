from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"
API = "http://localhost:8080/api/v1"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)

SPRING = "http://localhost:8080/api/v1"

# -------------------------
# Fetch supabase tables
# -------------------------

def fetch_pins():
    res = supabase.table("user_pinned_courses").select("*").execute()
    return res.data


def fetch_old_courses():
    res = supabase.table("coursenew").select("*").execute()
    return res.data


# -------------------------
# Build lookup maps
# -------------------------

def build_maps():

    old_courses = fetch_old_courses()

    new_courses = requests.get(f"{SPRING}/courses").json()
    new_users = requests.get(f"{SPRING}/users").json()

    # old course_id → title
    old_course_map = {
        c["id"]: c["title"].strip().lower()
        for c in old_courses
    }

    # title → new course id
    new_course_map = {
        c["title"].strip().lower(): c["id"]
        for c in new_courses
    }

    # old user UUID → new user PK
    user_map = {
        u["userId"]: u["id"]
        for u in new_users
    }

    return old_course_map, new_course_map, user_map


# -------------------------
# Mapping helpers
# -------------------------

def map_course(old_id, old_course_map, new_course_map):

    title = old_course_map.get(old_id)

    if not title:
        raise Exception(f"Old course id {old_id} missing")

    new_id = new_course_map.get(title)

    if not new_id:
        raise Exception(f"Course '{title}' not found in new DB")

    return new_id


def map_user(uuid, user_map):

    new_id = user_map.get(uuid)

    if not new_id:
        raise Exception(f"User {uuid} not found")

    return new_id


# -------------------------
# Convert row
# -------------------------

def convert(row, old_course_map, new_course_map, user_map):

    return {
        "user": {
            "id": map_user(row["user_id"], user_map)
        },
        "course": {
            "id": map_course(row["course_id"], old_course_map, new_course_map)
        },
        "pinnedAt": row["pinned_at"]
    }


# -------------------------
# Upload
# -------------------------

def upload(payload):

    r = requests.post(
        f"{SPRING}/pinned-courses",
        json=payload
    )

    if r.status_code != 200:
        print(r.text)

    return r.status_code

# -------------------------
# Main
# -------------------------

def main():

    old_course_map, new_course_map, user_map = build_maps()

    rows = fetch_pins()

    for row in rows:

        payload = convert(row, old_course_map, new_course_map, user_map)

        status = upload(payload)

        print(row["user_id"], row["course_id"], status)


if __name__ == "__main__":
    main()