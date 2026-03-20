from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"

SPRING_API = "http://localhost:8080/api/v1/professors"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)


def fetch_professors():
    res = supabase.table("professorsnew").select("*").execute()
    return res.data


def convert(row):
    return {
        "name": row.get("name"),
        "designation": row.get("designation"),
        "department": row.get("department"),
        "email": row.get("email"),
        "researchInterests": row.get("research_interests")
    }


def upload(row):
    payload = convert(row)

    r = requests.post(SPRING_API, json=payload)

    print(row["id"], r.status_code)


def main():
    rows = fetch_professors()

    for row in rows:
        upload(row)


if __name__ == "__main__":
    main()
