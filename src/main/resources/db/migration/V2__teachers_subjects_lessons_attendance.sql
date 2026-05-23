create table teachers (
    id bigserial primary key,
    full_name varchar(200) not null unique
);

create table subjects (
    id bigserial primary key,
    name varchar(150) not null unique
);

create table lessons (
    id bigserial primary key,
    teacher_id bigint not null,
    subject_id bigint not null,
    group_id bigint not null,
    lesson_date date not null,
    pair_number int not null,

    constraint fk_lessons_teacher foreign key (teacher_id) references teachers(id),
    constraint fk_lessons_subject foreign key (subject_id) references subjects(id),
    constraint fk_lessons_group foreign key (group_id) references student_groups(id),

    constraint uk_lessons_unique_slot unique (teacher_id, group_id, lesson_date, pair_number)
);

create index idx_lessons_date on lessons(lesson_date);
create index idx_lessons_group on lessons(group_id);
create index idx_lessons_teacher on lessons(teacher_id);

create table attendance (
    id bigserial primary key,
    lesson_id bigint not null,
    student_id bigint not null,
    present boolean not null,

    constraint fk_attendance_lesson foreign key (lesson_id) references lessons(id),
    constraint fk_attendance_student foreign key (student_id) references students(id),

    constraint uk_attendance_lesson_student unique (lesson_id, student_id)
);

create index idx_attendance_lesson on attendance(lesson_id);
create index idx_attendance_student on attendance(student_id);

