-- Sample Users & Sessions
INSERT INTO chat_session (id, user_id, name, favorite, created_at)
VALUES
  ('a1111111-1111-1111-1111-111111111111', 'user1', 'AI Project Discussion', false, NOW()),
  ('a2222222-2222-2222-2222-222222222222', 'user1', 'Daily Standup Notes', true, NOW()),
  ('b1111111-1111-1111-1111-111111111111', 'user2', 'Bank Payment Flow', false, NOW()),
  ('b2222222-2222-2222-2222-222222222222', 'user2', 'Production Issue RCA', true, NOW());

-- Messages for AI Project Discussion
INSERT INTO chat_message (id, session_id, sender, content, context, created_at)
VALUES
  (gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'user', 'Can we improve our RAG model?', 'retrieval context: doc embeddings', NOW()),
  (gen_random_uuid(), 'a1111111-1111-1111-1111-111111111111', 'assistant', 'Yes, by optimizing the embedding search parameters.', NULL, NOW());

-- Messages for Daily Standup Notes
INSERT INTO chat_message (id, session_id, sender, content, context, created_at)
VALUES
  (gen_random_uuid(), 'a2222222-2222-2222-2222-222222222222', 'user', 'Yesterday I fixed API rate limit issue.', NULL, NOW()),
  (gen_random_uuid(), 'a2222222-2222-2222-2222-222222222222', 'assistant', 'Good! Today we can focus on exception logging.', NULL, NOW());

-- Messages for Bank Payment Flow
INSERT INTO chat_message (id, session_id, sender, content, context, created_at)
VALUES
  (gen_random_uuid(), 'b1111111-1111-1111-1111-111111111111', 'user', 'Explain how RTGS settlement happens.', 'context: payment core docs', NOW()),
  (gen_random_uuid(), 'b1111111-1111-1111-1111-111111111111', 'assistant', 'RTGS transactions are settled individually in real time.', NULL, NOW());

-- Messages for Production Issue RCA
INSERT INTO chat_message (id, session_id, sender, content, context, created_at)
VALUES
  (gen_random_uuid(), 'b2222222-2222-2222-2222-222222222222', 'user', 'Why did the batch job fail?', 'logs from job scheduler', NOW()),
  (gen_random_uuid(), 'b2222222-2222-2222-2222-222222222222', 'assistant', 'It failed due to DB connection timeout under heavy load.', NULL, NOW());