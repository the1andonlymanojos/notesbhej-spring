from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"

SPRING_API = "http://localhost:8080/api/v1/users"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)

def fetch_users():
    res = supabase.rpc(
        "get_users_with_email"
    ).execute()
    return res.data


def convert(row):
    return {
        "userId": row["user_id"],
        "fullName": row["full_name"],
        "email": row["email"],
        "profilePictureUrl": row["profile_picture_url"],
        "adminRequest": row["admin_request"],
        "batch": row["batch"],
        "role": row["role"].upper(),
        "createdAt": row["created_at"],
        "updatedAt": row["updated_at"]
    }


def upload(row):
    payload = convert(row)

    r = requests.post(SPRING_API, json=payload)

    print(row["user_id"], r.status_code)


def main():
    rows = fetch_users()

    for row in rows:
        upload(row)


if __name__ == "__main__":
    main()