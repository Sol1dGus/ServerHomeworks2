create table users (
    id bigserial primary key,
    username varchar(100) not null unique,
    password varchar(255) not null,
    role varchar(20) not null check (role in ('STUDENT', 'TEACHER', 'ADMIN')),
    student_id bigint,
    teacher_id bigint,
    created_at timestamp not null default now(),

    constraint fk_users_student foreign key (student_id) references students(id) on delete set null,
    constraint fk_users_teacher foreign key (teacher_id) references teachers(id) on delete set null,
    constraint uk_users_student unique (student_id),
    constraint uk_users_teacher unique (teacher_id)
);

create index idx_users_role on users(role);
