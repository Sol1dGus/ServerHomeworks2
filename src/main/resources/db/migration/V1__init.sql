create table student_groups (
    id bigserial primary key,
    name varchar(100) not null unique
);

create table students (
    id bigserial primary key,
    full_name varchar(200) not null unique,
    group_id bigint not null,
    constraint fk_students_group foreign key (group_id) references student_groups(id)
);

create index idx_students_group_id on students(group_id);

