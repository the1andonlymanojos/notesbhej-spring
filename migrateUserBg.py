from supabase import create_client
import requests

SUPABASE_URL = "https://gcjwijdzlhvzqtdnnwaq.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdjandpamR6bGh2enF0ZG5ud2FxIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0NTY3NTIyNSwiZXhwIjoyMDYxMjUxMjI1fQ.qwNW3lXoXix1dx6-ZDTqJucFBfKYfGLcLkWuafv6w2E"

SPRING_API = "http://localhost:8080/api/v1/users"

supabase = create_client(SUPABASE_URL, SUPABASE_KEY)


def get_all_users():
    return requests.get(SPRING_API).json()


def get_bg_prefs():
    res = supabase.table("userbgpref").select("*").execute()
    return res.data


def main():
    users = get_all_users()
    bg_rows = get_bg_prefs()

    # userId (UUID) -> DB id (Long)
    id_map = {u["userId"]: u["id"] for u in users}

    # userId -> bg
    bg_map = {
        row["user_id"]: row["bg"]
        for row in bg_rows
        if row["user_id"] and row["bg"]
    }

    for user_id, bg in bg_map.items():
        if user_id not in id_map:
            print("SKIP (not in spring):", user_id)
            continue

        db_id = id_map[user_id]

        url = f"{SPRING_API}/{db_id}"
        payload = {"bgPref": bg}

        r = requests.patch(url, json=payload)

        if r.status_code != 200:
            print("FAIL:", user_id, r.status_code, r.text)
        else:
            print("OK:", user_id)


if __name__ == "__main__":
    main()