-- Demo účty personálu, heslo všetkých: casnik2026
INSERT INTO staff_user (username, password_hash, role) VALUES
    ('casnik', '$2a$10$4q2lLgd3DXCFtSdI.flPFOWXQ9v72ToyQXU0EyJyyQXGrjf.H.0Wi', 'WAITER'),
    ('kuchyna', '$2a$10$mXLfzC2Xbny8X86FooCsn.EKd/UhvckARJ1x9Mb89EnrYjxA7S9i.', 'KITCHEN'),
    ('spravca', '$2a$10$umG6g1woenct2w/BO5r7a.S2GAgUwUbv/4bm3pigP7o84Xkvf/UL6', 'ADMIN');