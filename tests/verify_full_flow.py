from playwright.sync_api import sync_playwright, expect
import time
import uuid
import base64
import os

def verify_full_flow():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        user_a_email = f"usera_{uuid.uuid4()}@example.com".lower()
        user_b_email = f"userb_{uuid.uuid4()}@example.com".lower()
        password = "password"

        # Helper to register user
        def register_user(name, email, pwd):
            print(f"Registering {name} with {email}")
            page.goto("http://localhost:8080/register")
            page.fill("input[name='nombre']", name)
            page.fill("input[name='email']", email)
            page.fill("input[name='password']", pwd)
            page.click("button[type='submit']")
            expect(page).to_have_url("http://localhost:8080/login")

        # Helper to login
        def login(email, pwd):
            print(f"Logging in {email}")
            page.goto("http://localhost:8080/login")
            page.fill("input[name='username']", email)
            page.fill("input[name='password']", pwd)
            page.click("button[type='submit']")
            expect(page).to_have_url("http://localhost:8080/dashboard")

        # Create a dummy image for upload
        def create_dummy_image(path):
            # Create a 1x1 white pixel png
            img_data = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAAAAAA6fptVAAAACklEQVR4nGNiAAAABgDNjd8qAAAAAElFTkSuQmCC")
            with open(path, "wb") as f:
                f.write(img_data)

        try:
            create_dummy_image("/tmp/profile_pic.png")

            # 1. Register User A & B
            register_user("User A", user_a_email, password)
            register_user("User B", user_b_email, password)

            # 2. Login User A, Profile Update
            login(user_a_email, password)

            print("Updating Profile...")
            page.goto("http://localhost:8080/profile")
            page.fill("input[name='nombre']", "User A Updated")
            page.set_input_files("input[type='file']", "/tmp/profile_pic.png")
            page.click("button:has-text('Guardar cambios')")

            expect(page.locator("text=Perfil actualizado correctamente.")).to_be_visible()

            # Verify Dashboard update
            page.goto("http://localhost:8080/dashboard")
            expect(page.locator("text=Hola, User A Updated")).to_be_visible()

            # Create Shared Account
            print("Creating Shared Account...")
            page.goto("http://localhost:8080/accounts/new")
            page.fill("input[name='nombre']", "Cuenta Compartida")
            page.select_option("select[name='tipo']", value="EFECTIVO")
            page.fill("input[name='saldoInicial']", "1000")
            page.check("#isShared")
            page.fill("input[name='sharedUserEmail']", user_b_email)
            page.click("button:has-text('Guardar')")

            # Logout
            page.click("button:has-text('Cerrar sesión')")
            context.clear_cookies()
            page.goto("http://localhost:8080/login")

            # 3. Login User B
            login(user_b_email, password)

            # Verify Account visible
            expect(page.locator("text=Cuenta Compartida")).to_be_visible()

            # Try to Edit Account (Assuming link exists, checking security)
            # Find the account link or button. The list has "Nueva" button but editing is via link?
            # In index.html list: no edit button shown in dashboard?
            # In accounts/list: yes.
            page.goto("http://localhost:8080/accounts")
            # Find Edit button for "Cuenta Compartida".
            # The row contains "Cuenta Compartida".
            # Verify if Edit button is present or if clicking it redirects/errors.
            # Assuming row structure.
            # Let's try to access edit URL directly if we can guess ID or find link.
            # Getting href from the edit button.
            edit_link = page.locator("li:has-text('Cuenta Compartida')").locator("a:has-text('Editar')")

            if edit_link.count() > 0:
                 # If link exists, try to click
                 print("Edit link found, trying to click...")
                 edit_link.click()
                 # Should redirect to list with error
                 expect(page.locator("text=Solo el propietario puede editar la cuenta.")).to_be_visible()
            else:
                 print("No edit link found (Good UI protection if implemented, but we are testing Controller Security)")
                 # If no link, good. But let's verify Controller security by manually constructing URL if possible?
                 # Hard to know UUID.
                 pass

            # Verify Add Expense Date
            print("Verifying Expense Date...")
            page.goto("http://localhost:8080/expenses/new")
            # Check date input value
            date_value = page.input_value("input[name='fecha']")
            print(f"Date value: {date_value}")
            if not date_value:
                raise Exception("Date field is empty!")

            # Logout
            page.click("button:has-text('Cerrar sesión')")
            context.clear_cookies()

            print("Verification successful!")
            page.screenshot(path="/home/jules/verification/verification_full.png", full_page=True)

        except Exception as e:
            print(f"Verification failed: {e}")
            page.screenshot(path="/home/jules/verification/failure_full.png")
            raise e
        finally:
            browser.close()
            if os.path.exists("/tmp/profile_pic.png"):
                os.remove("/tmp/profile_pic.png")

if __name__ == "__main__":
    verify_full_flow()
