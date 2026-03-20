from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"

SPRING_API = "http://localhost:8080/api/v1/courses"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)


def fetch_courses():
    res = supabase.table("coursenew").select("*").execute()
    return res.data


def convert(row):
    return {
        "title": row.get("title"),
        "code": row.get("code"),
        "abbreviation": row.get("abbreviation"),
        "createdAt": row.get("created_at")
    }


def upload(course):
    payload = convert(course)

    r = requests.post(SPRING_API, json=payload)

    if r.status_code >= 300:
        print("FAILED", course["id"], r.text)
    else:
        print("OK", course["id"])


def main():
    rows = fetch_courses()

    for row in rows:
        upload(row)


if __name__ == "__main__":
    main()
